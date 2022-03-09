import sbtcrossproject.CrossPlugin.autoImport.{ crossProject, CrossType }

name := "Hyper Loop Runner"

version := "1.4"

lazy val ld47 =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("."))
  .settings(Seq(
    scalaVersion := "3.1.1",
    libraryDependencies ++= List(
      "eu.joaocosta"                 %%% "minart-core"    % "0.4.0-SNAPSHOT",
      "eu.joaocosta"                 %%% "minart-backend" % "0.4.0-SNAPSHOT",
      "eu.joaocosta"                 %%% "minart-pure"    % "0.4.0-SNAPSHOT",
      "eu.joaocosta"                 %%% "minart-image"   % "0.4.0-SNAPSHOT",
    ),
    scalacOptions in Test ++= Seq("-unchecked", "-deprecation")
  ))
  .jsSettings(Seq(
    scalaJSUseMainModuleInitializer := true
  ))
  .nativeSettings(Seq(
    nativeLinkStubs := true,
    nativeMode := "release-full",
    nativeLTO := "thin",
    nativeGC := "commix"
  ))
  .settings(name := "ld47")
