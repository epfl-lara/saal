import inox.{Context, DefaultReporter, Reporter, utils}
import stainless.Main.libPaths
import stainless.extraction.ExtractionPipeline
import stainless.extraction.xlang.trees as xt
import stainless.frontend.{CallBack, UserFiltering}
import stainless.frontends.dotc.*
import stainless.utils.CheckFilter
import stainless.{AbstractReport, MainHelpers, extraction, frontend, frontends}

import java.io.File

class StainlessSymbols(val symbols: xt.Symbols)(using ctx: Context) {
  // Only keeping necessary definitions
  lazy val filtered: StainlessSymbols = StainlessSymbols(UserFiltering().transform(symbols))
  lazy val transformed: extraction.trees.Symbols = extraction.pipeline.extract(symbols)._1
}

object StainlessSymbols {
  def extract(files: Seq[File]): StainlessSymbols = {
    val reporter = DefaultReporter(Set.empty)
    val ctx = Context(reporter, new utils.InterruptManager(reporter))
    val compilerFactory = new frontends.dotc.DottyCompiler.Factory(Nil, libPaths)
    val cb = new ExtractionCallback
    val front = compilerFactory(ctx, files.map(_.getAbsolutePath), cb)
    front.run()
    front.join()
    StainlessSymbols(cb.symbols)(using ctx)
  }

  private class ExtractionCallback extends CallBack {
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
}