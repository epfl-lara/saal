import inox.{DefaultReporter, utils}
import stainless.Main.libPaths
import stainless.extraction.xlang.trees as xt
import stainless.frontend.CallBack
import stainless.frontends.dotc.*
import stainless.{AbstractReport, MainHelpers, frontend, frontends}

object Main {

  val compilerFactory = new frontends.dotc.DottyCompiler.Factory(Nil, libPaths)

  class ExtractionCallback extends CallBack {
    var symbols: xt.Symbols = xt.NoSymbols
    override def beginExtractions(): Unit = ()

    override def apply(file: String,
                       unit: xt.UnitDef,
                       classes: Seq[xt.ClassDef],
                       functions: Seq[xt.FunDef],
                       typeDefs: Seq[xt.TypeDef]): Unit = {
      symbols = symbols.withClasses(classes).withFunctions(functions).withTypeDefs(typeDefs)
    }

    override def failed(): Unit = ()

    override def endExtractions(): Unit = ()

    override def stop(): Unit = ()

    override def join(): Unit = ()

    override def getReport: Option[AbstractReport[_]] = None
  }

  def createContext: inox.Context = {
    val reporter = new DefaultReporter(Set.empty)
    inox.Context(reporter, new utils.InterruptManager(reporter))
  }

  @main
  def theMain: Unit = {
    val ctx = createContext
    val args: Seq[String] = Seq("src/main/resources/HelloStainless.scala")
    val cb = new ExtractionCallback
    val front = compilerFactory(ctx, args, cb)
    front.run()
    front.join()
    // cb.symbols contains all symbols, including the library
    println(cb.symbols)
    example(cb.symbols)
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