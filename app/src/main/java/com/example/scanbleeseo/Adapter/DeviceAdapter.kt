package com.example.scanbleeseo.Adapter


import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.scanbleeseo.R

class DeviceAdapter (private val deviceList: ArrayList<Device>, private val onClick: ((selectedDevice: Device) -> Unit)? = null) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {



    // Représente les données
    data class Device (
        var name: String?,
        var mac: String?,
        var device: BluetoothDevice
    ) {
        override fun equals(other: Any?): Boolean {
            // On compare les MAC, pour ne pas ajouté deux fois le même device dans la liste.
            return other is Device && other.mac == this.mac
        }
    }

    // Comment s'affiche ma vue
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun showItem(device: Device, onClick: ((selectedDevice: Device) -> Unit)? = null) {
            itemView.findViewById<TextView>(R.id.title_list).text = device.name

            if (onClick != null) {
                itemView.setOnClickListener {
                    onClick(device)
                }
            }
        }
    }

    // Retourne une « vue » / « layout » pour chaque élément de la liste
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }

    // Connect la vue ET la données
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.showItem(deviceList[position], onClick)
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

}

