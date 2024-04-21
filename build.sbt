import sbtcrossproject.CrossPlugin.autoImport.{ crossProject, CrossType }
import scala.scalanative.build._

name := "Hyper Loop Runner"

version := "1.6"

lazy val ld47 =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("."))
  .settings(Seq(
    scalaVersion := "3.3.3",
    libraryDependencies ++= List(
      "eu.joaocosta" %%% "minart" % "0.6.0-M3",
    )
  ))
  .jsSettings(Seq(
    scalaJSUseMainModuleInitializer := true
  ))
  .nativeSettings(
    Seq(
      nativeConfig ~= {
        _
        .withLinkStubs(true)
        .withMode(Mode.releaseFull)
        .withLTO(LTO.thin)
        .withGC(GC.commix)
        .withEmbedResources(true)
      }
    )
  )
  .settings(name := "ld47")
