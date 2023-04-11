package com.example.skillbox_project_10
import android.media.Ringtone
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue



@Parcelize
//так как в Parcelable нет переменной типа Ringtone ее нужно
//добавить через @RawValue, чтобы сделать ее Parcelable
class Ring (private val ring: @RawValue Ringtone):Parcelable{

    fun getRingtone(): Ringtone {
        return ring
    }

}