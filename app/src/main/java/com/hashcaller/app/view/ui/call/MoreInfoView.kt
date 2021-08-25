package com.hashcaller.app.view.ui.call

import android.os.Parcel
import android.os.Parcelable

class MorePhone : Parcelable {
    var name: String?

    constructor(`in`: Parcel) {
        name = `in`.readString()
    }

    constructor(name: String?) {
        this.name = name
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        val CREATOR: Parcelable.Creator<MorePhone?> = object : Parcelable.Creator<MorePhone?> {
            override fun createFromParcel(`in`: Parcel): MorePhone? {
                return MorePhone(`in`)
            }

            override fun newArray(size: Int): Array<MorePhone?> {
                return arrayOfNulls(size)
            }
        }
    }
}