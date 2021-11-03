package com.example.view

import com.example.controller.MainController
import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.geometry.Rectangle2D
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import tornadofx.View
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO


class MainView : View("Screenshoter") {
    val mainController: MainController by inject()
    override val root: VBox by fxml("/screenview.fxml")

    val cb_min: CheckBox by fxid()
    val sd_dur: Slider by fxid()
    val canvas: Canvas by fxid()
    val image: ImageView by fxid()
    val canvaszone: AnchorPane by fxid()
    val brushsize: TextField by fxid()
    val eraser: CheckBox by fxid()
    val colorPick: ColorPicker by fxid()
    val g = canvas.graphicsContext2D

    val save = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)
    val fast_save = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
    val make_screen = KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)


    init {
        image.fitWidth = canvaszone.width
        image.fitHeight = canvaszone.height
        canvas.width = image.fitWidth
        canvas.height = image.fitHeight
    }

    fun Combi(){
        primaryStage.scene.accelerators[fast_save] = Runnable {
            on_save()
        }
        primaryStage.scene.accelerators[save] = Runnable {
            on_save_as()
        }
        primaryStage.scene.accelerators[make_screen] = Runnable {
            evt_snap()
        }
    }

    fun painting(){
        canvas.onMouseDragged = EventHandler { e ->
            val size = brushsize.text.toDouble()
            val x = e.x - size / 2
            val y = e.y - size / 2
            if(image.image != null) {
                if (eraser.isSelected) {
                    g.clearRect(x, y, size, size)
                } else {
                    g.fill = colorPick.value
                    g.fillRect(x, y, size, size)
                }
            }
        }

    }

    fun setimg(){
        g.clearRect(0.0, 0.0, canvas.width, canvas.height)
        val img = SwingFXUtils.toFXImage(
            Robot().createScreenCapture(Rectangle(Toolkit.getDefaultToolkit().screenSize)),
            null
        )
        image.fitWidth = img.width / 2
        image.fitHeight = img.height / 2
        image.image = img
        canvas.width = image.fitWidth
        canvas.height = image.fitHeight
    }

    fun evt_snap(){
        //println(sd_dur.value)
        if (cb_min.isSelected) {
            primaryStage.isIconified = true
            Thread.sleep(sd_dur.value.toLong())
            setimg()
            primaryStage.isIconified = false
        }
        else {
            Thread.sleep(sd_dur.value.toLong())
            setimg()
        }
    }

    fun on_exit(){
        Platform.exit()
    }

    /*fun handle(event: KeyEvent) {
        if (event.code == KeyCode.S && event.isControlDown) {
            on_save()
        }
    }*/


    fun on_save(){
        if(image.image != null) {
            var ssp = SnapshotParameters()
            ssp.viewport = Rectangle2D(canvaszone.layoutX, canvaszone.layoutY, image.fitWidth, image.fitHeight)
            val image: WritableImage = canvaszone.snapshot(ssp, null)
            val file: File = File("C:\\Users\\Corey\\Desktop\\pic.png")
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file)
            } catch (e: IOException) {
                //println("Failed to save pic: $e")
                val alert = Alert(AlertType.ERROR)
                alert.title = "Error"
                alert.headerText = null
                alert.contentText = "Failed to save pic: $e"
                alert.showAndWait()
            }
        }
        else{
            val alert = Alert(AlertType.INFORMATION)
            alert.title = "Error"
            alert.headerText = null
            alert.contentText = "Plz do a screanshot or open file!"
            alert.showAndWait()
        }
    }

    fun on_close(){
        g.clearRect(0.0, 0.0, canvas.width, canvas.height)
        image.fitWidth = canvaszone.width
        image.fitHeight = canvaszone.height
        image.image = null
        canvas.width = image.fitWidth
        canvas.height = image.fitHeight
    }

    fun on_open(){
        val fileChooser = FileChooser()
        fileChooser.title = "Select Pictures"
        fileChooser.extensionFilters.addAll( //
            FileChooser.ExtensionFilter("All Files", "*.*"),
            FileChooser.ExtensionFilter("JPG", "*.jpg"),
            FileChooser.ExtensionFilter("PNG", "*.png")
        )
        try {
            val file = fileChooser.showOpenDialog(primaryStage)
            if (file != null) {
                val bufferedImage = ImageIO.read(file)
                g.clearRect(0.0, 0.0, canvas.width, canvas.height)
                val img = SwingFXUtils.toFXImage(bufferedImage, null)
                image.fitWidth = img.width / 2
                image.fitHeight = img.height / 2
                image.image = img
                canvas.width = image.fitWidth
                canvas.height = image.fitHeight
            }
        }catch (e: RuntimeException) {
            val alert = Alert(AlertType.ERROR)
            alert.title = "Error"
            alert.headerText = null
            alert.contentText = "Failed to open pic: $e"
            alert.showAndWait()
        }
    }

    fun on_save_as(){
        if(image.image != null) {
            val directoryChooser = DirectoryChooser()
            directoryChooser.title = "Select dir"
            val dir = directoryChooser.showDialog(primaryStage)
            if (dir != null) {
                var ssp = SnapshotParameters()
                ssp.viewport = Rectangle2D(canvaszone.layoutX, canvaszone.layoutY, image.fitWidth, image.fitHeight)
                val image: WritableImage = canvaszone.snapshot(ssp, null)
                val file: File = File(dir.toString().plus("\\pic.png"))
                println(dir.toString().plus("\\pic.png"))

                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file)
                } catch (e: IOException) {
                    //println("Failed to save pic: $e")
                    val alert = Alert(AlertType.ERROR)
                    alert.title = "Error"
                    alert.headerText = null
                    alert.contentText = "Failed to save pic: $e"
                    alert.showAndWait()
                }
            }
        }
        else{
            val alert = Alert(AlertType.INFORMATION)
            alert.title = "Error"
            alert.headerText = null
            alert.contentText = "Plz do a screanshot or open file!"
            alert.showAndWait()
        }
    }

    fun show_hotkeys(){
        val alert = Alert(AlertType.INFORMATION)
        alert.title = "Hotkeys"
        alert.headerText = null
        alert.contentText = "CTRL+F - ScreenShot\nCTRL+SHIFT+S - Save\nCTRL+S - Fast Save"
        alert.showAndWait()
    }


    /*override val root = vbox {
        alignment = Pos.CENTER
        label(mainController.labelText) {
            //bind(mainController.labelText)
            addClass(Styles.heading)
        }
        button("Click!"){
            action {
                mainController.getRandomName()
            }
        }
    }*/

}
