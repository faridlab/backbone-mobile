package id.startapp.pheromone.core.mapper

import kotlinx.serialization.Serializable

// Three-way mapper contract: domain entity ↔ serializable DTO ↔ form input
interface BaseEntityMapper<E, DTO, FormData> {
    fun toDto(entity: E): DTO
    fun toDomain(dto: DTO): E
    fun toEntity(formData: FormData): E
}

// Generic list wrapper for paginated DTO responses
@Serializable
data class ListDTO<DTO>(
    val items: List<DTO>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
)
