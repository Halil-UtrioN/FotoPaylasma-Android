/*
 * Copyright [2024] [Halil İbrahim ÇAKIR]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
Author/Developer: Halil İbrahim ÇAKIR
GitHub: https://github.com/Halil-UtrioN
NOTE: The project is mixed in Turkish and English.
*/

package com.cakirhalil.fotopaylama.fragmnet

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.cakirhalil.fotopaylama.R
import com.cakirhalil.fotopaylama.databinding.FragmentYuklemeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.UUID

@Suppress("UNREACHABLE_CODE")
class YuklemeFragment : Fragment() {
    private var _binding: FragmentYuklemeBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent> //Galeriye gitmek için Launcher
    private lateinit var permissionLauncher: ActivityResultLauncher<String> //izin isteği için Launcher
    var secilenGorsel: Uri? = null // Seçtiğimiz görselin URI'si
    var secilenBitmap: Bitmap? = null // Seçtiğimiz görseli Bitmap hali
    private lateinit var auth: FirebaseAuth // Firebase Authentication
    private lateinit var storage: FirebaseStorage // Firebase Storage
    private lateinit var db: FirebaseFirestore  // Firebase Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore
        registerLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYuklemeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.resimsecBTN.setOnClickListener { resimSec(it) } //Resim Seç Butonu
        binding.gonderBTN.setOnClickListener { gonder(it) } //Gönder Butonu

    }
    //onViewCreated Fonksyonları
    fun resimSec(view: View) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Read media images
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // İzin yok
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                    // İzinin açıklamasını göster
                    Snackbar.make(view, "Galeriye gitmek için izin vermelisiniz.", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver"
                    , View.OnClickListener {
                        // İzin iste
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                } else {
                    // İzin iste
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 1)
                }
            } else {
                // İzin var
                //Galeriye git
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery) // Galeriye gitmek için Launcher'ı başlatıyoruz.
            }
        } else {
            // Read external storage
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // İzin yok
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // İzinin açıklamasını göster
                    Snackbar.make(view, "Galeriye gitmek için izin vermelisiniz.", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver"
                        , View.OnClickListener {
                            // İzin iste
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                } else {
                    // İzin iste
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                }
            } else {
                // İzin var
                //Galeriye git
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery) // Galeriye gitmek için Launcher'ı başlatıyoruz.
            }
        }
    }
    fun gonder(view: View) {
        val uuid = UUID.randomUUID().toString() // Rastgele bir ID oluşturuyoruz.
        val reference = storage.reference // Firestore kök dizinini alıyoruz.
        val gorselReference = reference.child("posts").child("${auth.currentUser!!.uid.toString().take(5)+"_"+uuid}.jpg") // Klasör ve dosya adını belirliyoruz.
        if(secilenGorsel != null) {
            gorselReference.putFile(secilenGorsel!!).addOnSuccessListener { uploadTask ->
                // Dosya URL alınacak.
                gorselReference.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    // Veri tabanına kayıt ediyoruz.
                    val postMap = hashMapOf<String, Any>() // Gönderiyi tutan veri yapısı, veri tabanı için.
                    //Veri yapısına verileri kaydediyoruz.
                    postMap.put("downloadUrl", downloadUrl) // Görselin URL'sini kaydediyoruz.
                    postMap.put("userEmail", auth.currentUser!!.email.toString()) // Kullanıcının email'ini kaydediyoruz.
                    postMap.put("comment", binding.commentTXT.text.toString()) // Kullanıcının yorumunu kaydediyoruz.
                    postMap.put("date", Timestamp.now()) // Gönderinin tarihini kaydediyoruz.

                    db.collection("posts").document("${auth.currentUser!!.uid.toString().take(5)+"_"+uuid}").set(postMap).addOnSuccessListener { documentReference ->
                        // Başarılı bir şekilde kaydedildiğinde yapılacak işlemler.
                        Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.toString()}")
                        val action = YuklemeFragmentDirections.actionYuklemeFragmentToFeedFragment()
                        Navigation.findNavController(view).navigate(action)
                    }.addOnFailureListener { e ->
                        // Hata durumunda yapılacak işlemler.
                        e.printStackTrace()
                        Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }.addOnFailureListener { exception ->
                // Hata durumunda yapılacak işlemler.
                exception.printStackTrace()
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Burada seçilen görseli teyit edip Bitmap'e çeviriyoruz.
    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    secilenGorsel = intentFromResult.data
                    try {
                        if(Build.VERSION.SDK_INT >= 28) {
                            // 28 sdk fden büyük versiyonlarda Bitmap oluşturma.
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, secilenGorsel!!) // Burada Decoder ile görseli Bitmap'e çeviriyoruz.
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.onizlemeIMG.setImageBitmap(secilenBitmap) // Burada görseli ImageView'e yüklüyoruz.
                        } else {
                            // 28 sdk den küçük versiyonlarda Bitmap oluşturma.
                            secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, secilenGorsel)
                            binding.onizlemeIMG.setImageBitmap(secilenBitmap) // Burada görseli ImageView'e yüklüyoruz.
                        }
                    } catch (e: Exception) {
                        // Hata durumunda yapılacak Log kaydetme işlemi.
                        e.printStackTrace()
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                // İzin verildi.
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery) // Galeriye gitmek için Launcher'ı başlatıyoruz.
            } else {
                // Kullanıcı iptal etti.
                Snackbar.make(requireView(), "İzin reddedildi.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}