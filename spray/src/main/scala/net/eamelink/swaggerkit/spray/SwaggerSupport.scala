package net.eamelink.swaggerkit.spray

import spray.json._
import net.eamelink.swaggerkit._

object SwaggerSupport extends SwaggerSupport

trait SwaggerSupport
  extends DefaultJsonProtocol
  with TypeProtocol
  with ParameterProtocol
  with ContainerItemsTypeProtocol
  with PropertyProtocol
  with SchemaProtocol
  with HttpMethodProtocol
  with ErrorProtocol
  with OperationProtocol
  with ApiProtocol
  with ResourceDeclarationProtocol
  with ApiDocumentationProtocol

trait TypeProtocol {
  this: DefaultJsonProtocol =>

  implicit def viaType[T <: Type]: JsonFormat[T] = new JsonFormat[T] {
    def write(obj: T) = JsString(obj.name)
    def read(json: JsValue) = deserializationError("reading types currently not supported")
  }

}

trait ParameterProtocol {
  this: TypeProtocol with DefaultJsonProtocol =>

  implicit val parameterFormat = jsonFormat8(Parameter)
}

trait ContainerItemsTypeProtocol {
  this: DefaultJsonProtocol =>

  implicit val itemsFormat = jsonFormat(ContainerItemsType, "$ref")
}

trait PropertyProtocol {
  this: TypeProtocol with ContainerItemsTypeProtocol with DefaultJsonProtocol =>

  implicit val singleFormat = jsonFormat(SingleTypeProperty, "type", "required", "description", "allowableValues")
  implicit val containerFormat = jsonFormat(ContainerTypeProperty.apply, "type", "required", "description", "allowableValues", "items")

  implicit object PropertyJsonFormat extends JsonFormat[Property] {
    def write(obj: Property): JsValue = obj match {
      case p: SingleTypeProperty => singleFormat.write(p)
      case p: ContainerTypeProperty => containerFormat.write(p)
      case x => serializationError("Could not serialize " + obj)
    }

    def read(json: JsValue) = deserializationError("reading types currently not supported")
  }
}

trait SchemaProtocol {
  this: PropertyProtocol with DefaultJsonProtocol =>

  implicit val schemaFormat = jsonFormat2(Schema)
}

trait HttpMethodProtocol  {
  this: DefaultJsonProtocol =>

  implicit object HttpJsonFormat extends JsonFormat[HttpMethod] {
    def write(obj: HttpMethod) = obj match {
      case GET => JsString("GET")
      case POST => JsString("POST")
      case PUT => JsString("PUT")
      case DELETE => JsString("DELETE")
      case PATCH => JsString("PATCH")
      case HEAD => JsString("HEAD")
    }

    def read(json: JsValue) = deserializationError("reading http method currently not supported")
  }

}

trait ErrorProtocol {
  this: DefaultJsonProtocol =>

  implicit val errorFormat = jsonFormat2(Error)
}

trait OperationProtocol {
 this: HttpMethodProtocol with ParameterProtocol with ErrorProtocol with DefaultJsonProtocol =>

  implicit val operationFormat = jsonFormat(Operation, "nickname", "httpMethod", "summary", "parameters", "notes", "errorResponses", "responseClass")
}

trait ApiProtocol {
  this: OperationProtocol with DefaultJsonProtocol =>

  implicit val apiFormat = jsonFormat3(Api)
}

trait ResourceDeclarationProtocol {
  this: ApiProtocol with SchemaProtocol with DefaultJsonProtocol =>

  implicit val resourceDeclarationFormat = jsonFormat8(ResourceDeclaration.apply)
}

trait ApiDocumentationProtocol {
  this: ResourceDeclarationProtocol with DefaultJsonProtocol=>

  implicit val apiDocumentationFormat = jsonFormat4(ApiDocumentation)
}
