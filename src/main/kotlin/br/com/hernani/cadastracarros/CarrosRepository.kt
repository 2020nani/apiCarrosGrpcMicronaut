package br.com.hernani.cadastracarros

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface CarrosRepository: JpaRepository<Carros, Long> {
    abstract fun existsByPlaca(placa: String?): Boolean

}
