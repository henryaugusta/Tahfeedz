package com.feylabs.tahfidz.Model

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.crowdfire.cfalertdialog.CFAlertDialog
import com.feylabs.tahfidz.R
import com.feylabs.tahfidz.Util.SharedPreference.Preference
import com.feylabs.tahfidz.Util.SubBottomSheet
import com.feylabs.tahfidz.Util.API_Endpoint
import com.feylabs.tahfidz.View.CorrectionDetailActivity
import kotlinx.android.synthetic.main.layout_detail_submission.view.*
import kotlinx.android.synthetic.main.list_submission.view.*
import kotlinx.android.synthetic.main.list_submission.view.sub_start_end
import org.json.JSONObject


class SubmissionAdapter : RecyclerView.Adapter<SubmissionAdapter.SubmissionHolder>() {

    var listData = mutableListOf<SubmissionModel>()
    lateinit var subDetailBottomSheet: SubBottomSheet

    fun setData(listSetter: MutableList<SubmissionModel>) {
        listData.clear()
        listData.addAll(listSetter)
        notifyDataSetChanged()
    }

    inner class SubmissionHolder(view: View) : RecyclerView.ViewHolder(view) {
        var sub_id = itemView.sub_id
        var sub_date = itemView.sub_date
        var sub_student_name = itemView.sub_student_name
        var sub_status = itemView.sub_status
        var sub_start_end = itemView.sub_start_end
        var btn_sub_detail = itemView.btnSeeDetail

        val submissionModel = listData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_submission, parent, false)
        return SubmissionHolder(view)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: SubmissionHolder, position: Int) {
        holder.sub_student_name.text = listData[position].studentName
        listData[position].id = "#SE" + listData[position].id
        holder.sub_id.text = listData[position].id
        holder.sub_date.text = listData[position].date
        var status_code = listData[position].status_code
        val status_text = listData[position].status_text

        var checkStatus = status_code.toInt()
        var scoreCategory = ""

        if (status_code != "1") {
            holder.sub_status.setBackgroundResource(R.drawable.bg_status_waiting)
        }

        holder.sub_status.text = status_text
        holder.sub_start_end.text = listData[position].start + "-\n" + listData[position].end

        holder.itemView.setOnClickListener { v ->
            try {
                //CONFIG BOTTOM NAVIGATION UI===========================================================
                val activity = holder.itemView.context as Activity
                subDetailBottomSheet = SubBottomSheet(activity)
                val subDetId = subDetailBottomSheet.sub_det_id
                val subDetBtnClose = subDetailBottomSheet.btnCloseBottom
                val subDetDate = subDetailBottomSheet.sub_det_date
                val subStartEnd = subDetailBottomSheet.sub_start_end
                val subStartStatus = subDetailBottomSheet.sub_det_status
                val subDetScoreSmall = subDetailBottomSheet.sub_det_score_small
                val subDetScoreBig = subDetailBottomSheet.sub_det_score_big
                val subDetMp3 = subDetailBottomSheet.sub_det_mp3View
                val btnConfDeleteSubmission = subDetailBottomSheet.btnConfDeleteSubmission
                val btnSeeCorrectionSubmission = subDetailBottomSheet.btnSeeCorrection
                val cardDelete = subDetailBottomSheet.deleteCard
                val subDetScoreCategory = subDetailBottomSheet.sub_det_category_score

                subDetailBottomSheet.sub_det_score_ahkam.text = listData[position].score_ahkam
                subDetailBottomSheet.sub_det_score_makhroj.text = listData[position].score_makhroj
                subDetailBottomSheet.sub_det_score_tajwid.text = listData[position].score_tajwid
                subDetailBottomSheet.sub_det_score_itqan.text = listData[position].score_itqan

                subDetailBottomSheet.show()

                val loginType = Preference(activity).getPrefString("login_type")
                if (loginType == "student") {
                    btnSeeCorrectionSubmission.text = "Lihat Catatan/Koreksi Pembimbing"
                } else {
                    cardDelete.visibility = View.GONE
                    btnSeeCorrectionSubmission.text = "Input/Edit Koreksi dan Nilai"
                }

                subDetBtnClose.setOnClickListener {
                    subDetailBottomSheet.dismiss()
                }

                when (checkStatus) {
                    0 -> {
                        subDetScoreBig.text = holder.itemView.context.getString(R.string.not_graded)
                        subDetScoreSmall.text =
                            holder.itemView.context.getString(R.string.not_graded)
                        subStartStatus.text = holder.itemView.context.getString(R.string.not_graded)
                        subDetScoreCategory.visibility = View.GONE

                    }
                    1 -> {
                        subStartStatus.text = "Sudah Dinilai"
                        subDetScoreBig.text = listData[position].score_final
                        subDetScoreSmall.text = listData[position].score_final
                        subDetScoreCategory.visibility = View.VISIBLE
                        subDetScoreCategory.text = scoreCategory
                    }
                }

                // SET UP UI FOR BOTTOM LAYOUT =========================================================
                subDetId.text = listData[position].id
                subStartEnd.text = "${listData[position].start} - ${listData[position].end}"
                subDetDate.text = listData[position].date


                var mp3Path = listData[position].audio
                    .replaceFirst(".", "")
                    .replaceFirst("/", "")
                    .replace(" ", "%20")



                btnSeeCorrectionSubmission.setOnClickListener {
                    val intent =
                        Intent(holder.itemView.context, CorrectionDetailActivity::class.java)
                    intent.apply {
                        putExtra("data", listData[position])
                        putExtra("url", subDetMp3.url)
                    }
                    subDetailBottomSheet.dismiss()
                    activity.startActivity(intent)
                }


                //SETTING UP MP3========================================================================
                subDetMp3.settings.javaScriptEnabled = true
                subDetMp3.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    subDetailBottomSheet.dismiss()
                    holder.itemView.context.startActivity(i)
                }
                subDetMp3.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        view?.loadUrl(API_Endpoint.MP3_MOBILE + mp3Path)
                        return true
                    }
                }
                subDetMp3.loadUrl(API_Endpoint.MP3_MOBILE + mp3Path)
                Log.i("MP3View URL", API_Endpoint.MP3_MOBILE + mp3Path)


                //DELETE SUBMISSION=====================================================================
                btnConfDeleteSubmission.setOnClickListener {
                    // Create Alert using Builder
                    val cfAlert = CFAlertDialog.Builder(activity)
                        .setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT)
                        .setTitle("Anda Yakin Ingin Menghapus Setoran Ini ??")
                        .setMessage("File yang sudah dihapus tidak dapat dikembalikan")
                        .addButton(
                            "HAPUS",
                            -1,
                            -1,
                            CFAlertDialog.CFAlertActionStyle.POSITIVE,
                            CFAlertDialog.CFAlertActionAlignment.END
                        ) { dialog, which ->
                            //Call Delete Submission Function
                            deleteSubmission(
                                listData[position].id.removeRange(0, 3),
                                position, activity
                            )
                            dialog.dismiss()
                        }
                        .addButton(
                            "BATAL", -1, -1, CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                            CFAlertDialog.CFAlertActionAlignment.END
                        ) { dialog, which ->
                            dialog.dismiss()
                        }
                    cfAlert.show()
                }
            } catch (e: Exception) {
            }
        }

    }


    private fun deleteSubmission(
        submissionID: String,
        position: Int,
        activity: Activity
    ) {
        //Showing Progress Animation
        val loading = activity.findViewById(R.id.anim_loading) as LinearLayout
        loading.visibility = View.VISIBLE
        Log.i("id_submission", submissionID)
        AndroidNetworking.post(API_Endpoint.DELETE_SUBMISSION)
            .addBodyParameter("submission_id", submissionID)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    loading.visibility = View.GONE
                    Log.i("FAN-delSub", response.toString())
                    if (response.getInt("response_code") == 1) {
                        Toast.makeText(
                            activity,
                            "Berhasil Menghapus Setoran",
                            Toast.LENGTH_LONG
                        ).show()

                        //Dismiss Bottom Nav
                        subDetailBottomSheet.visibility = View.GONE

                        //Remove Submission From Adapter and listData
                        listData.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemChanged(position)
                    } else {
                        Toast.makeText(
                            activity,
                            "Gagal Menghapus Setoran, Hubungi Admin untuk menghapus setoran",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onError(anError: ANError) {
                    loading.visibility = View.GONE
                    Toast.makeText(
                        activity,
                        "Terjadi Gangguan Koneksi , Gagal Menghapus Setoran",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}