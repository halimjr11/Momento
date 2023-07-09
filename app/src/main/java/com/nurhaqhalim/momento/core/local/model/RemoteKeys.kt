package com.nurhaqhalim.momento.core.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nurhaqhalim.momento.utils.GlobalConstants

@Entity(tableName = GlobalConstants.remoteKeys)
data class RemoteKeys(
    @PrimaryKey val id: String,
    val prevKey: Int?,
    val nextKey: Int?
)
