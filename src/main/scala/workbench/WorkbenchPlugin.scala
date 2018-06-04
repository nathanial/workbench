package com.lihaoyi.workbench
import java.io.File

import sbt._
import sbt.Keys._
import autowire._
import org.scalajs.sbtplugin.ScalaJSPlugin

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global

object WorkbenchPlugin extends AutoPlugin {

  override def requires = WorkbenchBasePlugin

  private def globFiles(dir: String, extension: String): List[File] = {
    val results = new ListBuffer[File]()
    for(file <- new File(dir).listFiles()){
      if(file.isFile && file.getName.endsWith(extension)){
        results.append(file)
      } else if(file.isDirectory){
        results.append(globFiles(file.getAbsolutePath, extension):_*)
      }
    }
    results.toList
  }

  object autoImport {
      val changedScalaFiles = taskKey[List[String]]("Provides the addresses of the Scala files that have changed")
      val refreshBrowsers = taskKey[Unit]("Sends a message to all connected web pages asking them to refresh the page")
  }
  import autoImport._
  import WorkbenchBasePlugin.server
  import ScalaJSPlugin.AutoImport._

  val modificationTimes = mutable.HashMap[String, Long]()

  val workbenchSettings = Seq(
    changedScalaFiles := {
      val streamsValue = streams.value
      var files: List[String] = Nil
      val scalaFiles = (
        globFiles((baseDirectory.value / "src").getAbsolutePath, ".scala")
          ++ globFiles((baseDirectory.value / "target/scala-2.12/src_managed").getAbsolutePath, ".scala")
        )
      for(x <- scalaFiles){
        println("workbench: Checking Scala" + x.getName)
        FileFunction.cached(streamsValue.cacheDirectory / x.getName, FilesInfo.lastModified, FilesInfo.lastModified) {
          (f: Set[File]) =>
            val fsPath = f.head.getAbsolutePath.drop(new File("").getAbsolutePath.length)
            files = fsPath :: files
            f
        }(Set(x))
      }
      files
    },

    refreshBrowsers := {
      streams.value.log.info("workbench: Reloading Pages...")
      val scalaFiles = changedScalaFiles.value
      for(file <- scalaFiles){
        println("Le File", file)
      }
      if(!scalaFiles.isEmpty){
        println("IT CHANGED")
        server.value.Wire[Api].reload().call()
      }
    },
    
    refreshBrowsers := refreshBrowsers.triggeredBy(fastOptJS in Compile).value
  )

  override def projectSettings = workbenchSettings

}
