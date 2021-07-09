package au.edu.swin.sdmd.l10_hardcell

import android.graphics.Bitmap
import android.graphics.Color

class ElemCA(val width: Int, val height: Int) {
    var code = 0
    private var rules = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
    private var ca: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    /**
     * Given a rule number, break it down into the rules.
     * @param code
     */
    fun setNumber(code: Int) {
        this.code = code
        val binary = Integer.toBinaryString(code)
        rules = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
        for (i in binary.length - 1 downTo 0) {
            if (binary[i] == '1') {
                rules[binary.length - i - 1] = 1
            } else {
                rules[binary.length - i - 1] = 0
            }
        }

    }

    /**
     * This method does most of the work. The first row in the bitmap is coloured white with
     * one black cell in the centre. For the remaining rows, each cell is coloured white or black
     * depending on the cell above it and to the left and right about it. The initial rule number
     * provided to the object determines the pattern of whether a cell is on (black) or off (white).
     */
    fun drawCA() {
        var color2: Int?
        var color1: Int?
        var color0: Int?
        for (j in 0 until width) {
            ca.setPixel(j, 0, Color.WHITE)
        }
        ca.setPixel(Math.round((width / 2).toFloat()), 0, Color.BLACK)
        for (i in 1 until height) {
            for (j in 0 until width) {
                if (j > 0) {
                    color2 = ca.getPixel(j - 1, i - 1)
                } else {
                    color2 = 0
                }
                color1 = ca.getPixel(j, i - 1)
                if (j < width - 1) {
                    color0 = ca.getPixel(j + 1, i - 1)
                } else {
                    color0 = 0
                }

                var number = 0
                if (color2 == Color.BLACK) number += 4
                if (color1 == Color.BLACK) number += 2
                if (color0 == Color.BLACK) number += 1

                if (rules[number] == 1) {
                    ca.setPixel(j, i, Color.BLACK)
                } else {
                    ca.setPixel(j, i, Color.WHITE)
                }
            }
        }
    }

    /**
     * @return the generated bitmap
     */
    fun getCA(): Bitmap {
        return ca
    }

    fun processCA(): Bitmap {
        drawCA()
        return ca
    }
}