# Adding a Module

A business module is a bounded context — it owns its domain, repositories, use cases, screens, and sync schema. The skeleton deliberately ships zero modules; this doc describes the pattern.

The running example here is a `catalog` module for product catalog browsing. Replace the name to taste.

## Module layout

Each module adds a subtree under every relevant layer. Keep the leaf directory name (`catalog` below) identical across layers so it's grep-able.

```
id.startapp.pheromone
├── domain/
│   └── catalog/
│       ├── entity/
│       │   ├── Product.kt
│       │   └── Category.kt
│       └── repository/
│           └── CatalogRepository.kt          interface only
│
├── application/
│   └── usecases/catalog/
│       ├── ListProductsUseCase.kt
│       └── GetProductUseCase.kt
│
├── infrastructure/
│   └── catalog/
│       ├── api/
│       │   └── CatalogApiClient.kt
│       ├── mapper/
│       │   └── ProductMapper.kt              DTO → entity
│       ├── repository/
│       │   └── CatalogRepositoryImpl.kt
│       └── sync/
│           └── CatalogSyncHandler.kt         optional
│
└── presentation/
    └── catalog/
        ├── ProductListScreen.kt
        ├── ProductDetailScreen.kt
        └── ProductListViewModel.kt
```

SQLDelight schemas for the module go under:

```
shared/src/commonMain/sqldelight/id/startapp/pheromone/catalog/
├── ProductQueries.sq
└── CategoryQueries.sq
```

## 1. Define the domain

```kotlin
// domain/catalog/entity/Product.kt
package id.startapp.pheromone.domain.catalog.entity

data class Product(
    val id: String,
    val name: String,
    val priceCents: Long,
    val categoryId: String,
)
```

```kotlin
// domain/catalog/repository/CatalogRepository.kt
package id.startapp.pheromone.domain.catalog.repository

import id.startapp.pheromone.domain.catalog.entity.Product
import id.startapp.pheromone.domain.types.Result

interface CatalogRepository {
    suspend fun list(limit: Int = 50, cursor: String? = null): Result<List<Product>>
    suspend fun get(id: String): Result<Product>
}
```

Rules:
- `domain` is pure Kotlin. No Ktor, no Android, no SQLDelight imports.
- Entities are `data class`es. Business logic lives on the entity when it's invariant (price formatting, status transitions).
- Repositories return `Result<T>` (sealed type in `domain/types/`), not exceptions.

## 2. Write the use case

```kotlin
// application/usecases/catalog/ListProductsUseCase.kt
package id.startapp.pheromone.application.usecases.catalog

import id.startapp.pheromone.domain.catalog.entity.Product
import id.startapp.pheromone.domain.catalog.repository.CatalogRepository
import id.startapp.pheromone.domain.types.Result

class ListProductsUseCase(private val repo: CatalogRepository) {
    suspend operator fun invoke(limit: Int = 50): Result<List<Product>> =
        repo.list(limit = limit)
}
```

One use case = one action. If you're tempted to pass flags, split into two.

## 3. Implement the infrastructure

HTTP client uses the shared authenticated `HttpClient`:

```kotlin
// infrastructure/catalog/api/CatalogApiClient.kt
package id.startapp.pheromone.infrastructure.catalog.api

import id.startapp.pheromone.core.api.BaseCrudApiClient
import id.startapp.pheromone.infrastructure.catalog.api.dto.ProductDto
import io.ktor.client.HttpClient

class CatalogApiClient(
    httpClient: HttpClient,
    baseUrl: String,
) : BaseCrudApiClient<ProductDto>(
    httpClient = httpClient,
    baseUrl = baseUrl,
    endpoint = "catalog/products",
)
```

Repository wires API + cache together:

```kotlin
// infrastructure/catalog/repository/CatalogRepositoryImpl.kt
class CatalogRepositoryImpl(
    private val api: CatalogApiClient,
    private val cacheDao: CacheDao,
    private val connectivityMonitor: ConnectivityMonitor,
) : CatalogRepository { /* ... */ }
```

For offline-first support, extend `OfflineFirstRepository<Product>` from `infrastructure/repository/`. For a simple online-only module, just wrap the API client.

## 4. Register with DI

Create a Koin module per business module and pass it to `initKoin`:

```kotlin
// infrastructure/catalog/CatalogModule.kt
package id.startapp.pheromone.infrastructure.catalog

import id.startapp.pheromone.domain.catalog.repository.CatalogRepository
import id.startapp.pheromone.infrastructure.catalog.api.CatalogApiClient
import id.startapp.pheromone.infrastructure.catalog.repository.CatalogRepositoryImpl
import id.startapp.pheromone.infrastructure.network.BuildConfig
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val catalogModule = module {
    single { CatalogApiClient(httpClient = get(named("authenticated")), baseUrl = BuildConfig.API_BASE_URL) }
    single<CatalogRepository> { CatalogRepositoryImpl(api = get(), cacheDao = get(), connectivityMonitor = get()) }

    factoryOf(::ListProductsUseCase)
    factoryOf(::GetProductUseCase)
}
```

Plug it in at the Android/iOS entry points. On Android edit `MobileApplication.kt`:

```kotlin
initKoin {
    androidContext(this@MobileApplication)
    androidLogger(Level.ERROR)
    modules(catalogModule)    // ← add module here
}
```

On iOS, pass it to `initKoinIOS` in `shared/src/iosMain/.../Main_ios.kt`.

## 5. Add the UI

```kotlin
// presentation/catalog/ProductListViewModel.kt
class ProductListViewModel(
    private val listProducts: ListProductsUseCase,
) : BaseViewModel<ProductListState, ProductListIntent, ProductListEffect>() {
    override fun initialState() = ProductListState.Loading

    override fun handleIntent(intent: ProductListIntent) = when (intent) {
        ProductListIntent.Load -> launch {
            when (val r = listProducts()) {
                is Result.Success -> setState { ProductListState.Loaded(r.data) }
                is Result.Error -> emitEffect(ProductListEffect.ShowError(r.error.message()))
            }
        }
    }
}
```

Screens use `koinInject()` / `koinViewModel()` and call `component.send(ProductListIntent.Load)` on composition. When you wire `MainActivity` to a real navigator (Decompose is a good KMP fit), add a child for the module.

## 6. (Optional) Register a sync handler

If the module mutates data offline, wire a sync handler so `OutboxManager` can drain its queue:

```kotlin
val catalogModule = module {
    // ... repositories above ...

    single { CatalogSyncHandler(api = get(), cacheDao = get()) }
}

// Extend SyncEntityRegistry registration at startup:
KoinPlatform.getKoin().get<SyncEntityRegistry>().register(
    KoinPlatform.getKoin().get<CatalogSyncHandler>()
)
```

## Conventions

- **Naming**: PascalCase file = PascalCase type inside. No `Impl` suffix on interfaces (`CatalogRepository`), always suffix implementations (`CatalogRepositoryImpl`).
- **Packages**: one package per bounded context at every layer. Do not share a package across modules.
- **Entity sharing**: never. If two modules need the same concept (e.g. `Money`), put it in `domain/shared/`, not in one of the modules.
- **Schema ownership**: the `.sq` file lives with the module. `AppDatabase.sq` only references tables defined in core or in modules bundled into this app.
- **Migrations**: when a module changes its schema, add a `.sqm` migration under `shared/src/commonMain/sqldelight/migrations/` and bump the version in `shared/build.gradle.kts` `sqldelight { database("AppDatabase") { ... } }`.

## Removing a module

1. Delete the module's subtree under each layer.
2. Delete its `.sq` files and any migration that referenced them.
3. Remove its Koin module from `initKoin { modules(...) }`.
4. Grep for its package name — nothing should remain.

Mirror this process in tests: deleting the module should not require edits to any other module.
