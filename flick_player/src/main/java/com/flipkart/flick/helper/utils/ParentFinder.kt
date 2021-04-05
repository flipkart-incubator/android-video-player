/*
 * Copyright (C) 2021 Flipkart Internet Pvt Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.flick.helper.utils

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment

@Suppress("UNCHECKED_CAST")
class ParentFinder<T> {
    private val clazz: Class<T>
    private val child: Any

    constructor(child: Fragment, clazz: Class<T>) {
        this.child = child
        this.clazz = clazz
    }

    constructor(child: Activity, clazz: Class<T>) {
        this.child = child
        this.clazz = clazz
    }

    constructor(child: Context, clazz: Class<T>) {
        this.child = child
        this.clazz = clazz
    }

    fun find(): T {
        var parent: T? = null
        if (this.child is Fragment) {
            parent = this.findRecursiveParentFragment(this.child, this.clazz)
            if (parent == null) {
                if (this.clazz.isInstance(this.child.activity)) {
                    parent = this.child.activity as T?
                } else if (this.clazz.isInstance(this.child.activity!!.applicationContext)) {
                    parent = this.child.activity!!.applicationContext as T
                }
            }
        } else if (this.child is Context && this.clazz.isInstance(this.child.applicationContext)) {
            parent = this.child.applicationContext as T
        }

        return parent
            ?: throw IllegalStateException("Either activity or parent fragment or application hosting " + this.child.javaClass.simpleName + " should implement " + this.clazz.simpleName)
    }

    private fun findRecursiveParentFragment(fragment: Fragment, clazz: Class<T>): T? {
        val parentFragment = fragment.parentFragment
        return if (parentFragment != null) {
            if (clazz.isInstance(parentFragment)) {
                parentFragment as T?
            } else {
                val grandParentFragment = parentFragment.parentFragment
                if (grandParentFragment != null) this.findRecursiveParentFragment(
                    parentFragment,
                    clazz
                ) else null
            }
        } else {
            null
        }
    }
}
