import inox.{DefaultReporter, utils}
import stainless.Main.libPaths
import stainless.extraction.xlang.trees as xt
import stainless.frontend.CallBack
import stainless.frontends.dotc.*
import stainless.{AbstractReport, MainHelpers, frontend, frontends}

import java.io.File

object Main {
  @main
  def theMain: Unit = {
    val symsWrapper = StainlessSymbols.extract(Seq(new File("src/main/resources/HelloStainless.scala")))
    println("Symbols:")
    println(symsWrapper.filtered.symbols)
    example(symsWrapper.filtered.symbols)
  }

  def example(syms: xt.Symbols): Unit = {
    import xt.*
    def peel(e: Expr, acc: Seq[BigInt]): Seq[BigInt] = e match {
      case ClassConstructor(ClassType(id: SymbolIdentifier, _), Seq(head: IntegerLiteral, tail)) if id.symbol.path == Seq("stainless", "collection", "Cons") =>
        peel(tail, acc :+ head.value)
      case ClassConstructor(ClassType(id: SymbolIdentifier, _), Seq()) if id.symbol.path == Seq("stainless", "collection", "Nil") =>
        acc
      case _ =>
        sys.error(s"Unexpected expression: $e")
    }

    val fd = syms.functions.find(_._1.name == "helloStainless").getOrElse(sys.error("helloStainless not found"))._2
    val peeled = peel(fd.fullBody, Seq.empty)
    println("Content of helloStainless as letters: "+peeled.map(_.toChar).mkString(""))
  }
}