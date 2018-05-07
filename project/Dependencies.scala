import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object Dependencies {

  // jvm dependencies
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.1.1"
  val akka = "com.typesafe.akka" %% "akka-actor" % "2.5.4"
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.4"

  // js and shared dependencies
  val autowire = Def.setting("com.lihaoyi" %%% "autowire" % "0.2.6")
  val dom = Def.setting("org.scala-js" %%% "scalajs-dom" % "0.9.3")
  val upickle = Def.setting("com.lihaoyi" %%% "upickle" % "0.4.4")

}
