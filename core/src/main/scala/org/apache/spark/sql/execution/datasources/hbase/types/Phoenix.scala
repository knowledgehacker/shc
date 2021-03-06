/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.datasources.hbase.types

import org.apache.hadoop.hbase.util.Bytes
import org.apache.phoenix.query.QueryConstants
import org.apache.phoenix.schema.{SortOrder, PDatum, RowKeySchema}
import org.apache.phoenix.schema.RowKeySchema.RowKeySchemaBuilder
import org.apache.phoenix.schema.types._
import org.apache.spark.sql.Row
import org.apache.spark.sql.execution.SparkSqlSerializer
import org.apache.spark.sql.execution.datasources.hbase._
import org.apache.spark.sql.types._

class Phoenix(f:Option[Field] = None) extends SHCDataType {

  def fromBytes(src: HBaseType): Any = {
    if (f.isDefined) {
      f.get.dt match {
        case ByteType => src(0)
        case BinaryType => src
        case _ => mapToPhoenixTypeInstance(f.get.dt).toObject(src)
      }
    } else {
      throw new UnsupportedOperationException(
        "Phoenix coder: without field metadata, 'bytesToColumn' conversion can not be supported")
    }
  }

  def toBytes(input: Any): Array[Byte] = {
    input match {
      case data: Boolean => PBoolean.INSTANCE.toBytes(data)
      case data: Byte => PTinyint.INSTANCE.toBytes(data)
      case data: Array[Byte] => data
      case data: Double => PDouble.INSTANCE.toBytes(data)
      case data: Float => PFloat.INSTANCE.toBytes(data)
      case data: Int => PInteger.INSTANCE.toBytes(data)
      case data: Long => PLong.INSTANCE.toBytes(data)
      case data: Short => PSmallint.INSTANCE.toBytes(data)
      case data: String => PVarchar.INSTANCE.toBytes(data)
      case _ => throw new UnsupportedOperationException(s"unsupported data type $input")
    }
  }

  private def mapToPhoenixTypeInstance(input: DataType): PDataType[_] = {
    input match {
      case BooleanType => PBoolean.INSTANCE
      case ByteType => PTinyint.INSTANCE
      case DoubleType => PDouble.INSTANCE
      case IntegerType => PInteger.INSTANCE
      case FloatType => PFloat.INSTANCE
      case LongType => PLong.INSTANCE
      case ShortType => PSmallint.INSTANCE
      case StringType => PVarchar.INSTANCE
      case BinaryType => PBinary.INSTANCE
      case _ => throw new UnsupportedOperationException(s"unsupported data type $input")
    }
  }
}
