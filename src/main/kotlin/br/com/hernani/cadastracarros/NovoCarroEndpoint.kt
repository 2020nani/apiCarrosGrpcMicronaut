package br.com.hernani.cadastracarros

import br.com.hernani.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class NovoCarroEndpoint(
    val carrosRepository: CarrosRepository
) : CarsServiceGrpc.CarsServiceImplBase() {
    val logger = LoggerFactory.getLogger(NovoCarroEndpoint::class.java)
    override fun cadastraCarros(request: CarsRequest?, responseObserver: StreamObserver<CarsResponse>?) {
        logger.info("Iniciando cadastro de carros")
        val validaPlaca = request?.placa?.matches("[A-Z]{3}-[0-9]{4}".toRegex())

        if(validaPlaca == false){
            logger.info("Placa invalida ${request?.placa}")
            return responseObserver!!.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("Placa invalida")
                    .withDescription("Formato da placa tem que ser letras e numeros,ex: AAA-9999")
                    .asRuntimeException()
            )
        }
        if (carrosRepository.existsByPlaca(request?.placa)) {
            logger.info("Ja e um carro cadastrado com a placa ${request?.placa}")
            return responseObserver!!.onError(
                Status.ALREADY_EXISTS
                    .withDescription("Carro com placa existente")
                    .asRuntimeException()
            )
        }

        try {
            val carros = request.converte()
            carrosRepository.save(carros)
            logger.info("Carro: ${carros.modelo}, ${carros.placa} cadastrado com sucesso")
            responseObserver?.onNext(
                CarsResponse.newBuilder()
                    .setId(carros.id!!)
                    .build()
            )
            responseObserver?.onCompleted()


        } catch (e: ConstraintViolationException) {
         logger.info("Request invalido")
            return responseObserver!!.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("Dados invalidos")
                    .asRuntimeException()
            )
        }

    }

    override fun excluiCarros(request: CarsId?, responseObserver: StreamObserver<ExcluiResponse>?) {

           carrosRepository.deleteById(request?.id)

        responseObserver?.onNext(ExcluiResponse.newBuilder()
            .setMessage("Carro com o id: ${request?.id} deletado com sucesso")
            .build())
        responseObserver?.onCompleted()
    }
}

private fun CarsRequest?.converte(): Carros {

    return Carros(this?.modelo!!, this?.placa!!)
}
