package gcp4zio

import org.slf4j.{Logger, LoggerFactory}
import zio.stream.{Sink, Stream, ZSink, ZStream}
import java.io.IOException

package object gcs {
  lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)
  type GCSEnv           = GCSApi
  type GCSStream        = Stream[IOException, Byte]
  type GCSStreamWithEnv = ZStream[GCSEnv, IOException, Byte]
  type GCSSink          = Sink[IOException, Byte, Byte, Long]
  type GCSSinkWithEnv   = ZSink[GCSEnv, IOException, Byte, Byte, Long]
}
