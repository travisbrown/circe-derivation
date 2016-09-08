package io.circe.examples.derivation.better

import export.reexports

@reexports[DerivedDecoder, DerivedObjectEncoder]
object auto extends ExportHookAdapter
