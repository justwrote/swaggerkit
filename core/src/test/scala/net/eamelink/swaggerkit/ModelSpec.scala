package net.eamelink.swaggerkit

import SimpleTypes._
import ContainerTypes._
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ModelSpec extends Specification {

  "The SchemaBuilder trait" should {
    "provide an implicit conversion from Type to Property" in {
      object t extends SchemaBuilder {
        val p: Property = Int
      }
      success
    }
  }

  "A Type" should {
    "have a name" in {
      val t: Type = Int
      t.name
      success
    }
  }

  "SimpleTypes" should {
    "contain all primitives from https://github.com/wordnik/swagger-core/wiki/Datatypes#primitives" in {
      val primitives: Seq[Type] = Seq(Byte, Boolean, Int, Long, Float, Double, String, Date)
      success
    }

    "have an Array Type constructor that takes a Type as parameter" in {
      val primitive: Type = Array(String)
      success
    }
  }

  "A Property" should {
    "take a description with 'is'" in {
      val p = Property(String) is "Real name"
      p.description must_== Some("Real name")
    }

    "take allowed values with 'allows'" in {
      val p = Property(String) allows ("Mr", "Dr", "Mrs", "Prof")
      p.allowableValues must beSome
    }

    "has a switch 'isRequired'" in new SampleParam {
      val p = Property(String)
      (p isRequired).required must_== true
    }

    "has a switch 'isOptional'" in new SampleParam {
      val p = Property(String)
      (p isOptional).required must_== false
    }
  }

  "The HttpMethod trait" should {
    "be extended by GET, POST, PUT, PATCH and DELETE" in {
      val methods: Seq[HttpMethod] = Seq(GET, POST, PUT, PATCH, DELETE)
      success
    }
  }

  "A Parameter" should {
    "have a factory QueryParam" in {
      QueryParam("foo", Int).paramType must_== "query"
    }

    "have a factory PathParam" in {
      PathParam("foo", Int).paramType must_== "path"
    }

    "have a factory BodyParam" in {
      BodyParam(Int).paramType must_== "body"
    }

    "take a description with 'is'" in new SampleParam {
      (param is "bar").description must beSome.which(_ == "bar")
    }

    "have a switch 'isRequired'" in new SampleParam {
      (param isRequired).required must_== true
    }

    "have a switch 'isOptional'" in new SampleParam {
      (param isOptional).required must_== false
    }

    "have a switch 'allowsMultiple'" in new SampleParam {
      (param allowsMultiple).allowMultiple must_== true
    }

    "have a switch 'noMultiple'" in new SampleParam {
      (param noMultiple).allowMultiple must_== false
    }

    "take a vararg values in 'withValues'" in new SampleParam {
      (param withValues ("a", "b", "c")).allowableValues must beSome
    }

    "take an enum with values in 'withValues'" in new SampleParam {
      object WeekDay extends Enumeration {
        type WeekDay = Value
        val Mon, Tue, Wed, Thu, Fri, Sat, Sun = Value
      }
      (param withValues (WeekDay)).allowableValues must beSome
    }

  }

  "An Operation" should {
    "take a vararg parameters with 'takes'" in new SampleOperation with SampleParam {
      (operation takes param).parameters must contain(param)
    }

    "take a note with 'note'" in new SampleOperation {
      (operation note "foobar").notes must beSome.which(_ == "foobar")
    }
  }

  "An Api" should {
    "take a description with 'describedBy'" in new SampleApi {
      (api describedBy "foobar").description must beSome.which(_ == "foobar")
    }

    "take a vararg operations in 'withOperations'" in new SampleApi with SampleOperation {
      (api withOperations operation).operations must contain(operation)
    }
  }
}

trait SampleParam extends Scope {
  val param = QueryParam("foo", Int)
}

trait SampleOperation extends Scope {
  val operation = Operation("foo", GET, "bar")
}

trait SampleApi extends Scope {
  val api = Api("foo")
}