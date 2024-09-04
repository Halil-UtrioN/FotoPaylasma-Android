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

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.cakirhalil.fotopaylama.R
import com.cakirhalil.fotopaylama.databinding.FragmentKayitBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class KayitFragment : Fragment() {

    private var _binding: FragmentKayitBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKayitBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            //Fragmnet geçis
            val action = KayitFragmentDirections.actionKayitFragmentToFeedFragment()
            view.findNavController().navigate(action)
        }

        binding.kayitBTN.setOnClickListener { kayitOl(it) } //Kayıt Ol Butonu
        binding.girisBTN.setOnClickListener { girisYap(it) } //Giriş Yap Butonu
        binding.googleSignBTN.setOnClickListener { googleSign(it) } //Google Giriş Butonu
    }
    //onViewCreated Fonksyonları
    fun kayitOl(view: View) {
        //Kayıt Ol butonu fonksyonu
        val email = binding.emailTXT.text.toString().trim()
        val sifre = binding.sifreTXT.text.toString()
        if(email.isNotEmpty() && sifre.isNotEmpty()) {
            //Firebase kayıt işlemi
            auth.createUserWithEmailAndPassword(email, sifre)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Kayıt", "createUserWithEmail:success")
                        val user = auth.currentUser
                        //Fragmnet geçis
                        val action = KayitFragmentDirections.actionKayitFragmentToFeedFragment()
                        view.findNavController().navigate(action)
                    } else {
                        Log.w("Kayıt", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "Lütfen boşlukları doldurunuz.", Toast.LENGTH_SHORT).show()
        }
    }
    fun girisYap(view: View) {
        //Giriş Yap butonu fonksyonu
        val email = binding.emailTXT.text.toString().trim()
        val sifre = binding.sifreTXT.text.toString()
        if(email.isNotEmpty() && sifre.isNotEmpty()) {
            //Firebase giriş işlemi
            auth.signInWithEmailAndPassword(email, sifre)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Giriş", "signInWithEmail:success")
                        val user = auth.currentUser
                        //Fragmnet geçis
                        val action = KayitFragmentDirections.actionKayitFragmentToFeedFragment()
                        view.findNavController().navigate(action)
                    } else {
                        Log.w("Giriş", "signInWithEmail:failure", task.exception)
                        Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "Lütfen boşlukları doldurunuz.", Toast.LENGTH_SHORT).show()
        }
    }
    fun googleSign(view: View) {
        //Google Giriş butonu fonksyonu

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}