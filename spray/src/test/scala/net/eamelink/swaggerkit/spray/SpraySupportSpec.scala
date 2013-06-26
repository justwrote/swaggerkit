package net.eamelink.swaggerkit.spray

import spray.json._
import net.eamelink.swaggerkit._
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class WritersSpec extends Specification with SampleApiDocumentation with SwaggerSupport {
  "The JSON output for a resource discovery page" should {

    val json = apiDoc.toJson.asJsObject

    "contain the base path" in {
      json.getFields("basePath").head must_== JsString(apiDoc.basePath)
    }

    "contain the swagger version" in {
      json.getFields("swaggerVersion").head must_== JsString(apiDoc.swaggerVersion)
    }

    "contain the api version" in {
      json.getFields("apiVersion").head must_== JsString(apiDoc.apiVersion)
    }

    "list the proper number of apis" in {
      json.getFields("apis").head match {
        case JsArray(elements) => elements must have size (1)
        case _ => failure("expected an array")
      }
    }

    "list the resource declaration path for a resource" in {
      json.getFields("apis").head match {
        case JsArray(elements) =>
          elements.head.asJsObject.getFields("path").head must_== JsString(apiDoc.apis.head.path)
        case _ => failure("expected an array")
      }
    }

    "list the description for a resource" in {
      json.getFields("apis").head match {
        case JsArray(elements) =>
          elements.head.asJsObject.getFields("description").head must_== JsString(apiDoc.apis.head.description)
        case _ => failure("expected an array")
      }
    }
  }
}

trait SampleApiDocumentation extends Scope with SchemaBuilder {
  lazy val apiDoc = ApiDocumentation(
    basePath = "http://api.example.com/",
    swaggerVersion = "1.1-SNAPSHOT",
    apiVersion = "1.1",
    apis = List(ResourceDeclaration(
      path = "/albums.{format}",
      description = "Operations on Albums",
      resourcePath = "/albums",
      basePath = "http://api.example.com/",
      swaggerVersion = "1.1-SNAPSHOT",
      apiVersion = "1.1",
      apis = List(
        albumsApi, albumApi),
      models = Map("Album" -> albumSchema))))

  lazy val albumsApi = Api("/albums") describedBy "An albums API" withOperations (listAlbums, createAlbum)
  lazy val albumApi = Api("/album/{albumId}") describedBy "An album API" withOperations (showAlbum, updateAlbum, deleteAlbum)

  lazy val listAlbums = Operation("listAlbums", GET, "List albums") takes (
    QueryParam("query", SimpleTypes.String) is "Filter by name",
    QueryParam("orderBy", SimpleTypes.String) is "The sort field. Defaults to 'id'" withValues ("id", "title")) // TODO: Maybe add sample data where this is populated by an Enumeration?

  lazy val createAlbum = Operation("createAlbum", POST, "Create a new album") takes (
    BodyParam(albumSchema))

  lazy val showAlbum = Operation("showAlbum", GET, "Show an album") takes (
    PathParam("albumId", SimpleTypes.String) is "The album id") note
    "This is just a sample note"

  lazy val updateAlbum = Operation("updateAlbum", PUT, "Update an album") takes () // TODO

  lazy val deleteAlbum = Operation("deleteAlbum", DELETE, "Delete an album") takes () // TODO

  lazy val albumSchema = Schema("Album") has (
    "id" -> SimpleTypes.Int,
    "title" -> SimpleTypes.String,
    "photos" -> ContainerTypes.Array(photoSchema))

  lazy val photoSchema = Schema("Photo") has (
    "id" -> SimpleTypes.Int,
    "title" -> SimpleTypes.String)
}