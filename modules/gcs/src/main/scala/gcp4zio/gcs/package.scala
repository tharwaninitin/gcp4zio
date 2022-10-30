package gcp4zio

import org.slf4j.{Logger, LoggerFactory}
import zio.stream.{Sink, Stream, ZSink, ZStream}
import java.io.IOException

package object gcs {
  private[gcs] lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)
  type TaskStream[+A]   = ZStream[Any, Throwable, A]
  type GCSStream        = Stream[IOException, Byte]
  type GCSStreamWithEnv = ZStream[GCS, IOException, Byte]
  type GCSSink          = Sink[IOException, Byte, Byte, Long]
  type GCSSinkWithEnv   = ZSink[GCS, IOException, Byte, Byte, Long]
}
