package gcp4zio.batch

import com.google.cloud.batch.v1.BatchServiceClient
import zio.{RIO, Scope, ZIO}

object BatchClient {

  /** Returns AutoCloseable BatchServiceClient object wrapped in ZIO
    * @return
    *   RIO[Scope, BatchServiceClient]
    */
  def apply(): RIO[Scope, BatchServiceClient] = ZIO.fromAutoCloseable(ZIO.attempt {
    BatchServiceClient.create()
  })
}
