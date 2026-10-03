package com.uzuu.diuchat.feature.base.presentation.viewmodel

interface BaseAction<State> {
    fun reduce(state: State): State
}
