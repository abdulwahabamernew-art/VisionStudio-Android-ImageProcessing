package com.visionstudio.app.navigation

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import com.visionstudio.app.CameraActivity
import com.visionstudio.app.VideoActivity
import com.visionstudio.app.ui.about.AboutScreen
import com.visionstudio.app.ui.appearance.AppearanceScreen
import com.visionstudio.app.ui.home.HomeScreen
import com.visionstudio.app.ui.image.*
import com.visionstudio.app.ui.lectures.LecturesScreen
import kotlinx.coroutines.launch

@Composable fun AppNavigation(){
 val nav=rememberNavController();val bar=remember{SnackbarHostState()};val scope=rememberCoroutineScope();val context=LocalContext.current
 Scaffold(snackbarHost={SnackbarHost(bar)}){padding->NavHost(nav,startDestination="home",modifier=Modifier.padding(padding)){
  composable("home"){HomeScreen(onNavigate={route->when(route){
   "camera"->context.startActivity(Intent(context,CameraActivity::class.java))
   "video"->context.startActivity(Intent(context,VideoActivity::class.java))
   "image","processing","detection","lectures","appearance","about"->nav.navigate(route)
   else->scope.launch{bar.showSnackbar("هذه الميزة غير متاحة في الإصدار الحالي")}
  }},onPending={title->scope.launch{bar.showSnackbar("$title غير متاح حاليًا")}})}
  composable("image"){ImageEditorScreen{nav.popBackStack()}}
  composable("processing"){FilterLabScreen{nav.popBackStack()}}
  composable("detection"){DetectionScreen{nav.popBackStack()}}
  composable("lectures"){LecturesScreen{nav.popBackStack()}}
  composable("appearance"){AppearanceScreen{nav.popBackStack()}}
  composable("about"){AboutScreen{nav.popBackStack()}}
 }}
}
