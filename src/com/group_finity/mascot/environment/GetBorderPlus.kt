package com.group_finity.mascot.environment

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.win.WindowHolder

class GetBorderPlus {

    companion object {

        private var allAreas = listOf<WindowHolder>()

        public fun windowsEnviromentTick(environment: Environment) {
            allAreas = environment.activesIE.reversed()
        }

        private fun hasContainsWindows(allAreas: List<WindowHolder>, mascot: Mascot): Boolean {
            val x = mascot.anchor.x
            val y = mascot.anchor.y
            return allAreas.any { it.area.contains(x, y) }
        }

        // --------- getFloor ----------
        fun getFloor(ignoreSeparator: Boolean, environment: MascotEnvironment, mascot: Mascot): Border {

            for (i in allAreas.indices) {
                val area = allAreas[i].area
                if (area.topBorder.isLowerThan(mascot.anchor)) continue

                if (area.topBorder.isOn(mascot.anchor)) {
                    if (!hasContainsWindows(allAreas.drop(i + 1), mascot) && checkWindowTop(mascot))
                        return area.topBorder
                    else break
                }
            }

            if (environment.workArea.bottomBorder.isOn(mascot.anchor)) {
                if (!ignoreSeparator || environment.isScreenTopBottom) {
                    return environment.workArea.bottomBorder
                }
            }
            return NotOnBorder.INSTANCE
        }

        private fun checkWindowTop(mascot: Mascot): Boolean {
            return (mascot.anchor.y - mascot.bounds.height > mascot.environment.workArea.top) &&
                    (mascot.anchor.x in (mascot.environment.workArea.left..mascot.environment.workArea.right)) &&
                    mascot.anchor.y > 0
        }

        // --------- getCeiling ----------
        fun getCeiling(ignoreSeparator: Boolean, environment: MascotEnvironment, mascot: Mascot): Border {

            for (i in allAreas.indices) {
                val area = allAreas[i].area
                if (area.bottomBorder.isHigherThan(mascot.anchor)) continue

                if (area.bottomBorder.isOn(mascot.anchor)) {
                    if (!hasContainsWindows(allAreas.drop(i + 1), mascot) && checkWindowBottom(mascot)) {
                        return area.bottomBorder
                    } else break
                }
            }

            if (environment.workArea.topBorder.isOn(mascot.anchor)) {
                if (!ignoreSeparator || environment.isScreenTopBottom) {
                    return environment.workArea.topBorder
                }
            }
            return NotOnBorder.INSTANCE
        }


        private fun checkWindowBottom(mascot: Mascot): Boolean {
            return (mascot.anchor.y + mascot.bounds.height <= mascot.environment.workArea.bottom) &&
                    (mascot.anchor.x in (mascot.environment.workArea.left..mascot.environment.workArea.right)) &&
                    mascot.anchor.y < mascot.environment.workArea.bottom

        }
        // --------- getWall ----------

        fun getWall(ignoreSeparator: Boolean, environment: MascotEnvironment, mascot: Mascot): Border {

            if (mascot.isLookRight) {

                for (i in allAreas.indices) {
                    val area = allAreas[i].area

                    if (area.leftBorder.isOn(mascot.anchor)) {
                        if (!hasContainsWindows(allAreas.drop(i + 1), mascot) && checkWindowWall(mascot)) {
                            return area.leftBorder
                        } else break
                    }
                }

                if (environment.workArea.rightBorder.isOn(mascot.anchor)) {
                    if (!ignoreSeparator || environment.isScreenLeftRight) {
                        return environment.workArea.rightBorder
                    }
                }


            } else {

                for (i in allAreas.indices) {
                    val area = allAreas[i].area

                    if (area.rightBorder.isOn(mascot.anchor)) {
                        if (!hasContainsWindows(allAreas.drop(i + 1), mascot) && checkWindowWall(mascot)) {
                            return area.rightBorder
                        } else break
                    }
                }

                if (environment.workArea.leftBorder.isOn(mascot.anchor)) {
                    if (!ignoreSeparator || environment.isScreenLeftRight) {
                        return environment.workArea.leftBorder
                    }
                }

            }
            return NotOnBorder.INSTANCE
        }

        private fun checkWindowWall(mascot: Mascot): Boolean {
            return (mascot.anchor.y in mascot.environment.workArea.top..mascot.environment.workArea.bottom) &&
                    (mascot.anchor.x in mascot.environment.workArea.left..mascot.environment.workArea.right)

        }

        fun hasFloorOnRange(ignoreSeparator: Boolean, xPos: Int, yFrom: Int, yTo: Int, mascot: Mascot): Int {

            for (i in allAreas.indices) {
                val area = allAreas[i].area
                val lv = area.topBorder.isOnLine(xPos, yFrom, yTo)
                if (lv == Int.MIN_VALUE) continue

                if (!hasContainsWindows(allAreas.drop(i + 1), mascot) && checkWindowTop(mascot)) {
                    return lv
                } else continue
            }

            val lv = mascot.environment.workArea.bottomBorder.isOnLine(xPos, yFrom, yTo)
            if (lv == Int.MIN_VALUE) return lv

            if (!ignoreSeparator || mascot.environment.isScreenTopBottom) return lv

            return Int.MIN_VALUE
        }

    }
}