package com.techart.reporter

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.techart.reporter.constants.Constants
import com.techart.reporter.constants.FireBaseUtils
import com.techart.reporter.model.Story
import com.techart.reporter.utils.TimeUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mAuth: FirebaseAuth? = null
    private var mStoryList: RecyclerView? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(loginIntent)
            }
        }
        mStoryList = findViewById(R.id.rv_story)
        mStoryList?.let {mStoryList?.setHasFixedSize(true)}
        val linearLayoutManager = LinearLayoutManager(this@MainActivity)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        mStoryList?.let {mStoryList?.setLayoutManager(linearLayoutManager)}

        fab.setOnClickListener { view ->
            val loginIntent = Intent(this@MainActivity, ReportActivity::class.java)
            startActivity(loginIntent)
            /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()*/
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        mAuth = FirebaseAuth.getInstance()

        nav_view.setNavigationItemSelectedListener(this)
        bindView();
    }

    override fun onStart() {
        super.onStart()
        mAuthListener?.let { mAuth?.addAuthStateListener(it) }
    }

    private fun bindView() {
        val fireBaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Story, StoryViewHolder>(
                Story::class.java, R.layout.item_storyrow, StoryViewHolder::class.java, FireBaseUtils.mDatabaseStory) {

            public override fun populateViewHolder(viewHolder: StoryViewHolder, model: Story, position: Int) {
                var postKey = getRef(position).key
                viewHolder.tvTitle.text = model.title
                if (model.category != null &&  model.province != null && model.status  != null)
                viewHolder.tvInfor.text = getString(R.string.info,model.category, model.province,model.status )
                viewHolder.tvDescription.text = model.description

                if (model.imageUrl != null) {
                    viewHolder.setIvImage(this@MainActivity, model.imageUrl)
                }
                viewHolder.setTint(this@MainActivity)


                if (model.numComments != null) {
                    viewHolder.tvNumComments.text = String.format("%s", model.numComments.toString())
                }

                if (model.numViews != null) {
                    viewHolder.tvNumViews.text = String.format("%s", model.numViews.toString())
                }

                if (model.timeCreated != null) {
                    val time = TimeUtils.timeElapsed(model.timeCreated)
                    viewHolder.tvTime.text  = time
                }

                viewHolder.setPostViewed(postKey)


                viewHolder.mView.setOnClickListener(View.OnClickListener {
                    addToViews(postKey!!, model)
                })


                viewHolder.btnComment.setOnClickListener(View.OnClickListener {
                    val commentIntent = Intent(this@MainActivity, CommentActivity::class.java)
                    commentIntent.putExtra(Constants.POST_KEY, postKey)
                    commentIntent.putExtra(Constants.POST_TITLE, model.getTitle())
                    startActivity(commentIntent)
                })
            }
        }
        mStoryList!!.adapter = fireBaseRecyclerAdapter
        fireBaseRecyclerAdapter.notifyDataSetChanged()
    }

    private fun addToViews(post_key: String, model: Story) {
        var mProcessView = true
        FireBaseUtils.mDatabaseViews.child(post_key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (mProcessView) {
                    if (dataSnapshot.hasChild(FireBaseUtils.getUiD())) {
                        mProcessView = false
                    } else {
                        mProcessView = false
                        FireBaseUtils.addStoryView(model, post_key)
                        FireBaseUtils.onStoryViewed(post_key)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_accidents -> {
                // Handle the camera action
                Toast.makeText(this@MainActivity,"This will be filtering the result to only accidents",Toast.LENGTH_LONG).show()
            }
            R.id.nav_campaigns -> {
                Toast.makeText(this@MainActivity,"This will be filtering the result to only Campaigns",Toast.LENGTH_LONG).show()
            }
            R.id.nav_corruption -> {
                Toast.makeText(this@MainActivity,"This will be filtering the result to only Corruption",Toast.LENGTH_LONG).show()
            }
            R.id.nav_littering -> {
                Toast.makeText(this@MainActivity,"This will be filtering the result to only Littering",Toast.LENGTH_LONG).show()
            }
            R.id.nav_share -> {
                Toast.makeText(this@MainActivity,"This will be filtering the result to only Traffic Violation",Toast.LENGTH_LONG).show()
            }
            R.id.nav_send -> {
                FirebaseAuth.getInstance().signOut();
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
