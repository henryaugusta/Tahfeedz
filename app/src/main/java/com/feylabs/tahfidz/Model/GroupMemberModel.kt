package com.feylabs.tahfidz.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GroupMemberModel(
    val student_id : String,
    val name : String,
    val student_contact : String,
    val mentor_id : String,
    val group_id : String,
    val group_name : String,
    val mentor_name : String,
    val mentor_contact : String
) : Parcelable