package top.learningman.enter

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class FloatingQSTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile
        tile.label = "Enter"
        if (!AccessibilityUtil.isFloatingButtonAvailable(this)) {
            tile.state = Tile.STATE_INACTIVE
        } else {
            if (ButtonWindowManager.isShowing()) {
                tile.state = Tile.STATE_ACTIVE
            } else {
                tile.state = Tile.STATE_INACTIVE
            }
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (AccessibilityUtil.isFloatingButtonAvailable(this)) {
            if (ButtonWindowManager.isShowing()) {
                val intent = Intent(this, ButtonAccessibilityService::class.java).apply {
                    putExtra(
                        ButtonAccessibilityService.TYPE_KEY,
                        ButtonAccessibilityService.REMOVE_VIEW
                    )
                }
                startService(intent)
                qsTile.state = Tile.STATE_INACTIVE
                qsTile.updateTile()
            } else {
                val intent = Intent(this, ButtonAccessibilityService::class.java).apply {
                    putExtra(
                        ButtonAccessibilityService.TYPE_KEY,
                        ButtonAccessibilityService.ADD_VIEW
                    )
                }
                startService(intent)
                qsTile.state = Tile.STATE_ACTIVE
                qsTile.updateTile()
            }
        } else {
            Toast.makeText(this, "Button is not available", Toast.LENGTH_SHORT).show()
            startActivityAndCollapse(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}