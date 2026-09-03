package com.stewstudio.app.engine

class DataModel : Instance(
    className = "DataModel",
    name = "Game"
) {
    val workspace = Instance(
        className = "Workspace",
        name = "Workspace"
    )

    init {
        addChild(workspace)
    }
}