package com.meida.sort

import com.meida.model.CommonData

import java.util.Comparator

class PinyinComparator : Comparator<CommonData> {

    override fun compare(o1: CommonData, o2: CommonData) = when {
        o1.letter == "@" || o2.letter == "#" -> -1
        o1.letter == "#" || o2.letter == "@" -> 1
        else -> o1.letter.compareTo(o2.letter)
    }

}
