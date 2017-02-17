// Copyright 2016 Foursquare Labs Inc. All Rights Reserved.

package io.fsq.rogue.query

import com.mongodb.{ReadPreference, WriteConcern}
import io.fsq.field.Field
import io.fsq.rogue.{Query, QueryHelpers, QueryOptimizer, Rogue, ShardingOk, !<:<}
import io.fsq.rogue.adapter.MongoClientAdapter
import io.fsq.rogue.types.MongoDisallowed


/** TODO(jacob): All of the collection methods implemented here should get rid of the
  *     option to send down a read preference, and just use the one on the query.
  */
class QueryExecutor[MongoCollection[_], Document, MetaRecord, Record, Result[_]](
  adapter: MongoClientAdapter[MongoCollection, Document, MetaRecord, Record, Result],
  optimizer: QueryOptimizer
) extends Rogue {

  def defaultWriteConcern: WriteConcern = QueryHelpers.config.defaultWriteConcern

  def count[M <: MetaRecord, State](
    query: Query[M, _, State],
    readPreferenceOpt: Option[ReadPreference] = None
  )(
    implicit ev: ShardingOk[M, State],
    ev2: M !<:< MongoDisallowed
  ): Result[Long] = {
    if (optimizer.isEmptyQuery(query)) {
      adapter.wrapEmptyResult(0L)
    } else {
      adapter.count(query, readPreferenceOpt)
    }
  }

  def countDistinct[M <: MetaRecord, FieldType, State](
    query: Query[M, _, State],
    readPreferenceOpt: Option[ReadPreference] = None
  )(
    field: M => Field[FieldType, M]
  )(
    implicit ev: ShardingOk[M, State],
    ev2: M !<:< MongoDisallowed
  ): Result[Long] = {
    if (optimizer.isEmptyQuery(query)) {
      adapter.wrapEmptyResult(0L)
    } else {
      adapter.countDistinct(query, field(query.meta).name, readPreferenceOpt)
    }
  }

  def distinct[M <: MetaRecord, FieldType, State](
    query: Query[M, _, State],
    readPreferenceOpt: Option[ReadPreference] = None
  )(
    field: M => Field[FieldType, M]
  )(
    implicit ev: ShardingOk[M, State], ev2: M !<:< MongoDisallowed
  ): Result[Seq[FieldType]] = {
    if (optimizer.isEmptyQuery(query)) {
      adapter.wrapEmptyResult(Vector.empty[FieldType])
    } else {
      adapter.distinct(query, field(query.meta).name, readPreferenceOpt)
    }
  }
}
