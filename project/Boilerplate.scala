import sbt._

object Boilerplate {
  def ccs(size: Int)(dir: File): Seq[File] = {
    val file = dir /  "io" / "circe" / "examples" / "derivation" / "definitions" / "ccs.scala"

    val header = """
      |package io.circe.examples.derivation.definitions
      |
      |case class LongWrapper(value: Long)
      |case class ShortWrapper(value: Short)
      |
    """.stripMargin

    val content = (0 to size).map { i =>
      val members = (1 to i).map { j =>
        val tpe = (j % 5) match {
          case 0 => "String"
          case 1 => "Int"
          case 2 => "Char"
          case 3 => "ShortWrapper"
          case _ => "LongWrapper"
        }

        f"m$j%02d: $tpe"
      }.mkString(", ")

      f"case class Cc$i%02d($members)"
    }.mkString("\n")

    IO.write(file, header ++ content)

    List(file)
  }

  def adts(size: Int)(dir: File): Seq[File] = {
    val file = dir /  "io" / "circe" / "examples" / "derivation" / "definitions" / "adts.scala"

    val header = """
      |package io.circe.examples.derivation.definitions
      |
      |""".stripMargin

    val content = (1 to size).map { i =>
      val base = f"sealed trait Base$i%02d"
      val members = (1 to i).map { j =>

        f"case class Bcc$i%02d$j%02d(s: String) extends Base$i%02d"
      }.mkString("\n")

      f"$base\n$members"
    }.mkString("\n\n")

    IO.write(file, header ++ content)

    List(file)
  }

  def deeps(size: Int)(dir: File): Seq[File] = {
    val file = dir /  "io" / "circe" / "examples" / "derivation" / "definitions" / "deeps.scala"

    val header = """
      |package io.circe.examples.derivation.definitions
      |
      |case class Deep00(s: String)
      |""".stripMargin

    val content = (1 to size).map { i =>
      f"case class Deep$i%02d(d: Deep${ i - 1 }%02d)"
    }.mkString("\n")

    IO.write(file, header ++ content)

    List(file)
  }

  def benches(size: Int, reps: Int)(dir: File): Seq[File] = {
    val raw     = dir /  "io" / "circe" / "examples" / "derivation" / "bench" / "Raw.scala"
    val simple  = dir /  "io" / "circe" / "examples" / "derivation" / "bench" / "Simple.scala"
    //val dryer   = dir /  "io" / "circe" / "examples" / "derivation" / "bench" / "Dryer.scala"
    val better  = dir /  "io" / "circe" / "examples" / "derivation" / "bench" / "Better.scala"
    val generic = dir /  "io" / "circe" / "examples" / "derivation" / "bench" / "Generic.scala"

    def header(name: String, short: String, deriverImport: String) = s"""
      |package io.circe.examples.derivation.bench
      |
      |import io.circe.examples.derivation.definitions._
      |import scala.concurrent.duration.Duration
      |import shapeless.test.compileTime
      |import $deriverImport
      |
      |object $name extends Bench("$short") {
      |  val size = $size
      |""".stripMargin

    def ccs(raw: Boolean) = (if (raw) (0 to math.min(size, 22)) else (0 to size)).map { i =>
      f"""    compileTime("io.circe.Decoder[Cc$i%02d]")"""
    }.mkString(",\n")

    val adts = (1 to size).map { i =>
      f"""    compileTime("io.circe.Decoder[Base$i%02d]")"""
    }.mkString(",\n")

    val deeps = (0 to size).map { i =>
      f"""    compileTime("io.circe.Decoder[Deep$i%02d]")"""
    }.mkString(",\n")

    def ccContent(raw: Boolean) = s"""
      |  val ccResults: List[Duration] = List(
      |${ List.fill(reps)(ccs(raw)).mkString(",\n") }
      |  )
      |""".stripMargin

    def adtContent(raw: Boolean) = if (!raw) s"""
      |  val adtResults: List[Duration] = List(
      |${ List.fill(reps)(adts).mkString(",\n") }
      |  )
      |""".stripMargin else "  val adtResults: List[Duration] = Nil"

    val deepContent = s"""
      |  val deepResults: List[Duration] = List(
      |${ List.fill(reps)(deeps).mkString(",\n") }
      |  )
      |""".stripMargin

    def content(raw: Boolean) = ccContent(raw) + adtContent(raw) + deepContent + "\n}"

    val rawImport     = "io.circe.examples.derivation.raw._"
    val simpleImport  = "io.circe.examples.derivation.simple._"
    val dryerImport   = "io.circe.examples.derivation.dryer.decoders._"
    val betterImport  = "io.circe.examples.derivation.better.auto._"
    val genericImport = "io.circe.generic.auto._"

    IO.write(raw, header("Raw", "raw macros", rawImport) + content(true))
    IO.write(simple, header("Simple", "simple Shapeless", simpleImport) + content(false))
    //IO.write(dryer, header("Dryer", "D", dryerImport) + content(false))
    IO.write(better, header("Better", "better Shapeless", betterImport) + content(false))
    IO.write(generic, header("Generic", "circe-generic", genericImport) + content(false))

    List(raw, simple, better, generic)
  }
}
