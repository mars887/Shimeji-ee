package com.group_finity.mascot.win

import com.sun.jna.Pointer

class WindowsMapper {
    companion object {
        fun applyTo(oldl: ArrayList<WindowHolder>, newl: ArrayList<WindowHolder>) {

            val out = ArrayList<WindowHolder>()

            for (h in newl) {
                if (contains(oldl, h.pointer)) {
                    out.add(
                        find(oldl, h.pointer).apply {
                            area.set(h.area.toRectangle())
                        }
                    )
                } else {
                    out.add(h)
                }
            }
            oldl.clear()
            oldl.addAll(out)
        }

        private fun contains(list: ArrayList<WindowHolder>, pointer: Pointer): Boolean {
            return list.any { it.pointer == pointer }
        }

        private fun find(list: ArrayList<WindowHolder>, pointer: Pointer): WindowHolder {
            return list.first { it.pointer == pointer }
        }
    }
}