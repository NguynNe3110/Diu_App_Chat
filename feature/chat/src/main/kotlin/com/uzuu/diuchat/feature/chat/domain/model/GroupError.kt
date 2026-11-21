package com.uzuu.diuchat.feature.chat.domain.model

sealed class GroupError(message: String) : Exception(message) {
    data object LastAdminCannotLeave :
        GroupError("Bạn là quản trị viên duy nhất còn lại trong nhóm. Vui lòng chuyển quyền quản trị cho thành viên khác trước khi rời nhóm.")
}
