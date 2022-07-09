package gcp4zio

import gcp4zio.utils.ApplicationLogger
import zio.stream.{Sink, Stream, ZSink, ZStream}
import java.io.IOException

package object gcs extends ApplicationLogger {
  type GCSEnv           = GCSApi
  type GCSStream        = Stream[IOException, Byte]
  type GCSStreamWithEnv = ZStream[GCSEnv, IOException, Byte]
  type GCSSink          = Sink[IOException, Byte, Byte, Long]
  type GCSSinkWithEnv   = ZSink[GCSEnv, IOException, Byte, Byte, Long]
}
