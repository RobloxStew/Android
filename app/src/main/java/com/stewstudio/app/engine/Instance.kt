package com.stewstudio.app.engine

open class Instance(
    var className: String,
    var name: String
) {
    var parent: Instance? = null
        private set

    private val children = mutableListOf<Instance>()

    fun addChild(child: Instance) {
        child.parent?.removeChild(child)

        children.add(child)
        child.parent = this
    }

    fun removeChild(child: Instance) {
        if (children.remove(child)) {
            child.parent = null
        }
    }

    fun getChildren(): List<Instance> {
        return children.toList()
    }

    fun findFirstChild(name: String): Instance? {
        return children.firstOrNull { it.name == name }
    }
}