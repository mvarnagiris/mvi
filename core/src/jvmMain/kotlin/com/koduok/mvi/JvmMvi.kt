package com.koduok.mvi

abstract class JvmMvi<INPUT, STATE>(initialState: STATE) : Mvi<INPUT, STATE>(initialState)