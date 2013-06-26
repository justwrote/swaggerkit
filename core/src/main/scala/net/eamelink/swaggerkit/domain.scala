package net.eamelink.swaggerkit

/**
 * Container for the entire API documentation.
 *
 * This is a model for the resource discovery page.
 */
case class ApiDocumentation(
  basePath: String,
  swaggerVersion: String,
  apiVersion: String,
  apis: List[ResourceDeclaration])

/**
 * Resource declaration, lists api's and model's for a given resource.
 */
case class ResourceDeclaration(
  // Properties shown on the resource discovery page.
  path: String,
  description: String,

  // Properties shown on the page for the given resource.
  resourcePath: String,
  basePath: String,
  swaggerVersion: String,
  apiVersion: String,
  apis: List[Api],
  models: Map[String, Schema])

object ResourceDeclaration {
  /**
   * Resource declaration constructor that finds all models in operations.
   */
  def apply(path: String, description: String, resourcePath: String, basePath: String, swaggerVersion: String, apiVersion: String, apis: List[Api]): ResourceDeclaration =
    ResourceDeclaration(path, description, resourcePath, basePath, swaggerVersion, apiVersion, apis, findModels(apis))

  /**
   * Find models that are references by the list of Api.
   *
   * Currently only looks for parameters, not operation return types.
   */
  private def findModels(apis: List[Api]): Map[String, Schema] = {
    apis.flatMap { api =>
      api.operations.flatMap { operation =>
        operation.parameters.map(_.dataType).collect {
          case schema: Schema => (schema.name -> schema)
        }
      }
    }.toMap
  }
}

/**
 * API, collection of operations on the same URL
 */
case class Api(
  path: String,
  description: Option[String] = None,
  operations: List[Operation] = Nil) {

  def describedBy(description: String) = copy(description = Some(description))
  def withOperations(ops: Operation*) = copy(operations = ops.toList)
}

/**
 * Http operation on a given url
 */
case class Operation(
  nickName: String,
  httpMethod: HttpMethod,
  summary: String,
  parameters: List[Parameter] = Nil,
  notes: Option[String] = None,
  errorResponses: List[Error] = Nil,
  responseClass: Option[String] = None) {

  def takes(params: Parameter*) = copy(parameters = params.toList)
  def note(note: String) = copy(notes = Some(note))
  def errors(errors: Error*) = copy(errorResponses = errors.toList)
}

case class Error(code: Int, reason: String)

/**
 * Http request parameter
 */
case class Parameter(
  name: Option[String],
  description: Option[String] = None,
  dataType: Type,
  required: Boolean = false,
  valueTypeInternal: Option[String] = None,
  allowMultiple: Boolean = false,
  allowableValues: Option[Seq[String]] = None,
  paramType: String) {

  def is(description: String) = copy(description = Some(description))
  def isRequired() = copy(required = true)
  def isOptional() = copy(required = false)
  def allowsMultiple() = copy(allowMultiple = true)
  def noMultiple() = copy(allowMultiple = false)
  def withValues(values: String*) = copy(allowableValues = Some(values))
  def withValues(enum: Enumeration) = copy(allowableValues = Some(enum.values.map(_.toString).toList))
}

/**
 * Factory for 'query' request parameters
 */
object QueryParam {
  def apply(name: String, dataType: Type) = Parameter(name = Some(name), dataType = dataType, paramType = "query", allowableValues = None)
}

/**
 * Factory for 'path' request parameters
 */
object PathParam {
  def apply(name: String, dataType: Type) = Parameter(name = Some(name), dataType = dataType, paramType = "path", required = true, allowableValues = None)
}

/**
 * Factory for 'body' request parameters
 */
object BodyParam {
  def apply(dataType: Type) = Parameter(None, dataType = dataType, paramType = "body", required = true)
}

/**
 * Http methods: GET, POST etc.
 */
sealed trait HttpMethod
case object GET extends HttpMethod
case object POST extends HttpMethod
case object PUT extends HttpMethod
case object PATCH extends HttpMethod
case object DELETE extends HttpMethod

/**
 * A property type from a 'model' in the Swagger output.
 *
 * This can be a primitive, complex or container type.
 */
sealed trait Type {
  def name: String
}

sealed trait SingleType extends Type

sealed trait ComplexType extends SingleType {
  def id: String
  def name = id
}
sealed trait PrimitiveType extends SingleType

sealed trait ContainerType extends Type {
  def underlying: SingleType
}

/**
 * A description of a complex type.
 */
case class Schema(id: String, properties: Option[Map[String, Property]] = None) extends ComplexType {
  def has(props: (String, Property)*) = copy(properties = Some(props.toMap))
}

/**
 * A property of a complex or container type.
 */
trait Property {
  def typ: Type
  def required: Boolean
  def description: Option[String]
  def allowableValues: Option[Seq[String]]

  def is(description: String): Property
  def allows(vals: String*): Property
  def isRequired(): Property
  def isOptional(): Property
}

private[swaggerkit] case class SingleTypeProperty(
  typ: SingleType,
  required: Boolean,
  description: Option[String],
  allowableValues: Option[Seq[String]]) extends Property {

  def is(description: String) = copy(description = Some(description))
  def allows(vals: String*) = copy(allowableValues = Some(vals.toSeq))
  def isRequired() = copy(required = true)
  def isOptional() = copy(required = false)
}

private[swaggerkit] case class ContainerItemsType(ref: String)
private[swaggerkit] object ContainerTypeProperty {
  def apply(
    typ: ContainerType,
    required: Boolean,
    description: Option[String],
    allowableValues: Option[Seq[String]]): ContainerTypeProperty =
    ContainerTypeProperty(typ, required, description, allowableValues, ContainerItemsType(typ.underlying.name))
}
private[swaggerkit] case class ContainerTypeProperty(
  typ: ContainerType,
  required: Boolean,
  description: Option[String],
  allowableValues: Option[Seq[String]],
  items: ContainerItemsType) extends Property {

  def is(description: String) = copy(description = Some(description))
  def allows(vals: String*) = copy(allowableValues = Some(vals.toSeq))
  def isRequired() = copy(required = true)
  def isOptional() = copy(required = false)
}

object Property {
  def apply(typ: Type,
    required: Boolean = false,
    description: Option[String] = None,
    allowableValues: Option[Seq[String]] = None): Property = typ match {
    case t: SingleType => SingleTypeProperty(t, required, description, allowableValues)
    case t: ContainerType => ContainerTypeProperty(t, required, description, allowableValues)
  }
}

/**
 * Predefined primitives
 *
 * https://github.com/wordnik/swagger-core/wiki/datatypes
 */
object SimpleTypes {
  object Void extends PrimitiveType { val name = "void" }
  object Byte extends PrimitiveType { val name = "byte" }
  object Boolean extends PrimitiveType { val name = "boolean" }
  object Int extends PrimitiveType { val name = "int" }
  object Long extends PrimitiveType { val name = "long" }
  object Float extends PrimitiveType { val name = "float" }
  object Double extends PrimitiveType { val name = "double" }
  object String extends PrimitiveType { val name = "string" }
  object Date extends PrimitiveType { val name = "Date" }
}

object ContainerTypes {
  case class Array(underlying: SingleType) extends ContainerType { val name = "Array" }
  case class List(underlying: SingleType) extends ContainerType { val name = "List" }
  case class Set(underlying: SingleType) extends ContainerType { val name = "Set" }
}