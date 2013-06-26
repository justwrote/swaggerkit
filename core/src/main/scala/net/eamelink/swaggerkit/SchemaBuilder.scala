package net.eamelink.swaggerkit

import scala.language.implicitConversions

/**
 * Trait with methods for building Schemas
 */
trait SchemaBuilder {
  implicit def typeToProperty(st: Type): Property = Property(st)
}