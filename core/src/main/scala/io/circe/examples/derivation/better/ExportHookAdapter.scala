package io.circe.examples.derivation.better

import io.circe.export.Exported
import export.Export5

/**
 * This is necessary only because circe has its own simplified
 * [[io.circe.export.Exported]] representation.
 */
trait ExportHookAdapter {
  implicit def convertExport5[A](implicit e5: Export5[A]): Exported[A] = Exported(e5.instance)
}
