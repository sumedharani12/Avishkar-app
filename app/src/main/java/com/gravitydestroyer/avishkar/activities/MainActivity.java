package com.gravitydestroyer.avishkar.activities;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.gravitydestroyer.avishkar.Avishweb;
import com.gravitydestroyer.avishkar.ContactActivity;
import com.gravitydestroyer.avishkar.Exhibition;
import com.gravitydestroyer.avishkar.R;
import com.gravitydestroyer.avishkar.ScheduleActivity;
import com.gravitydestroyer.avishkar.adapters.PostsAdapter;
import com.gravitydestroyer.avishkar.enums.PostStatus;
import com.gravitydestroyer.avishkar.enums.ProfileStatus;
import com.gravitydestroyer.avishkar.managers.DatabaseHelper;
import com.gravitydestroyer.avishkar.managers.PostManager;
import com.gravitydestroyer.avishkar.managers.ProfileManager;
import com.gravitydestroyer.avishkar.managers.listeners.OnObjectExistListener;
import com.gravitydestroyer.avishkar.model.Post;
import com.gravitydestroyer.avishkar.utils.AnimationUtils;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private PostsAdapter postsAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private ProfileManager profileManager;
    private PostManager postManager;
    private int counter;
    private TextView newPostsCounterTextView;
    private PostManager.PostCounterWatcher postCounterWatcher;
    private boolean counterAnimationInProgress = false;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.spacex2)
                .addProfiles(
                        new ProfileDrawerItem().withName("Avishkar 2018").withEmail("avskr.in").withIcon(getResources().getDrawable(R.drawable.avishkar_ico_round))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();


        //Now create your
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Home");
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName("OfflineEvents");
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName("OnlineEvents");
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(4).withName("Workshops");
        PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIdentifier(5).withName("Exhibitions");
        PrimaryDrawerItem item6 = new PrimaryDrawerItem().withIdentifier(6).withName("Schedule");
        PrimaryDrawerItem item7 = new PrimaryDrawerItem().withIdentifier(7).withName("Sponsors");
        PrimaryDrawerItem item8 = new PrimaryDrawerItem().withIdentifier(8).withName("Map");
        PrimaryDrawerItem item9 = new PrimaryDrawerItem().withIdentifier(9).withName("Contacts");
        PrimaryDrawerItem item10= new PrimaryDrawerItem().withIdentifier(10).withName("Visit Our Website");
        PrimaryDrawerItem item11 = new PrimaryDrawerItem().withIdentifier(11).withName("About AVISHKAR");




//create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)

                .withToolbar(toolbar)
                .addDrawerItems(
                        item1,
                        item2,
                        item3,
                        item4,
                        item5,
                        item6,
                        item7,
                        item8,
                        item9,
                        item10,
                        item11

                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D

                        switch (position) {
                                case 5:
                                    Intent exhi=new Intent(MainActivity.this,Exhibition.class);
                                    startActivity(exhi);
                                    break;
                            case 6:
                                Intent schedl=new Intent(MainActivity.this,ScheduleActivity.class);
                                startActivity(schedl);
                                break;
                            case 9:
                                Intent con=new Intent(MainActivity.this,ContactActivity.class);
                                startActivity(con);
                                break;

                            case 10:
                                    Intent avish=new Intent(MainActivity.this,Avishweb.class);
                                    startActivity(avish);
                                    break;

                        }return true;
                    }
                })
                .build();

        profileManager = ProfileManager.getInstance(this);
        postManager = PostManager.getInstance(this);
        initContentView();

        postCounterWatcher = new PostManager.PostCounterWatcher() {
            @Override
            public void onPostCounterChanged(int newValue) {
                updateNewPostCounter();
            }
        };

        postManager.setPostCounterWatcher(postCounterWatcher);

//        setOnLikeAddedListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNewPostCounter();
    }

    private void setOnLikeAddedListener() {
        DatabaseHelper.getInstance(this).onNewLikeAddedListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                counter++;
                showSnackBar("You have " + counter + " new likes");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST:
                    refreshPostList();
                    break;
                case CreatePostActivity.CREATE_NEW_POST_REQUEST:
                    refreshPostList();
                    showFloatButtonRelatedSnackBar(R.string.message_post_was_created);
                    break;

                case PostDetailsActivity.UPDATE_POST_REQUEST:
                    if (data != null) {
                        PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.POST_STATUS_EXTRA_KEY);
                        if (postStatus.equals(PostStatus.REMOVED)) {
                            postsAdapter.removeSelectedPost();
                            showFloatButtonRelatedSnackBar(R.string.message_post_was_removed);
                        } else if (postStatus.equals(PostStatus.UPDATED)) {
                            postsAdapter.updateSelectedPost();
                        }
                    }
                    break;
            }
        }
    }

    private void refreshPostList() {
        postsAdapter.loadFirstPage();
        if (postsAdapter.getItemCount() > 0) {
            recyclerView.scrollToPosition(0);
        }
    }

    private void initContentView() {
        if (recyclerView == null) {
            floatingActionButton = (FloatingActionButton) findViewById(R.id.addNewPostFab);

            if (floatingActionButton != null) {
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (hasInternetConnection()) {
                            addPostClickAction();
                        } else {
                            showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
                        }
                    }
                });
            }

            newPostsCounterTextView = (TextView) findViewById(R.id.newPostsCounterTextView);
            newPostsCounterTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshPostList();
                }
            });

            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
            recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            postsAdapter = new PostsAdapter(this, swipeContainer);
            postsAdapter.setCallback(new PostsAdapter.Callback() {
                @Override
                public void onItemClick(final Post post, final View view) {
                    PostManager.getInstance(MainActivity.this).isPostExistSingleValue(post.getId(), new OnObjectExistListener<Post>() {
                        @Override
                        public void onDataChanged(boolean exist) {
                            if (exist) {
                                openPostDetailsActivity(post, view);
                            } else {
                                showFloatButtonRelatedSnackBar(R.string.error_post_was_removed);
                            }
                        }
                    });
                }

                @Override
                public void onListLoadingFinished() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onAuthorClick(String authorId, View view) {
                    openProfileActivity(authorId, view);
                }

                @Override
                public void onCanceled(String message) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            recyclerView.setAdapter(postsAdapter);
            postsAdapter.loadFirstPage();
            updateNewPostCounter();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    hideCounterView();
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        }
    }

    private void hideCounterView() {
        if (!counterAnimationInProgress && newPostsCounterTextView.getVisibility() == View.VISIBLE) {
            counterAnimationInProgress = true;
            AlphaAnimation alphaAnimation = AnimationUtils.hideViewByAlpha(newPostsCounterTextView);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    counterAnimationInProgress = false;
                    newPostsCounterTextView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            alphaAnimation.start();
        }
    }

    private void showCounterView() {
        AnimationUtils.showViewByScaleAndVisibility(newPostsCounterTextView);
    }

    private void openPostDetailsActivity(Post post, View v) {
        Intent intent = new Intent(MainActivity.this, PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, post.getId());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(R.id.postImageView);
            View authorImageView = v.findViewById(R.id.authorImageView);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(MainActivity.this,
                            new android.util.Pair<>(imageView, getString(R.string.post_image_transition_name)),
                            new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name))
                    );
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST, options.toBundle());
        } else {
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
        }
    }

    public void showFloatButtonRelatedSnackBar(int messageId) {
        showSnackBar(floatingActionButton, messageId);
    }

    private void addPostClickAction() {
        ProfileStatus profileStatus = profileManager.checkProfile();

        if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
            openCreatePostActivity();
        } else {
            doAuthorization(profileStatus);
        }
    }

    private void openCreatePostActivity() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivityForResult(intent, CreatePostActivity.CREATE_NEW_POST_REQUEST);
    }

    private void openProfileActivity(String userId) {
        openProfileActivity(userId, null);
    }

    private void openProfileActivity(String userId, View view) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            View authorImageView = view.findViewById(R.id.authorImageView);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(MainActivity.this,
                            new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name)));
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST, options.toBundle());
        } else {
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST);
        }
    }

    private void updateNewPostCounter() {
        Handler mainHandler = new Handler(this.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                int newPostsQuantity = postManager.getNewPostsCounter();

                if (newPostsCounterTextView != null) {
                    if (newPostsQuantity > 0) {
                        showCounterView();

                        String counterFormat = getResources().getQuantityString(R.plurals.new_posts_counter_format, newPostsQuantity, newPostsQuantity);
                        newPostsCounterTextView.setText(String.format(counterFormat, newPostsQuantity));
                    } else {
                        hideCounterView();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.profile:
                ProfileStatus profileStatus = profileManager.checkProfile();

                if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    openProfileActivity(userId);
                } else {
                    doAuthorization(profileStatus);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
