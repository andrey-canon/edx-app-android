package org.proversity.edx.mobile.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;

import org.proversity.edx.mobile.core.IEdxEnvironment;
import org.proversity.edx.mobile.model.api.CourseEntry;
import org.proversity.edx.mobile.model.api.EnrolledCoursesResponse;
import org.proversity.edx.mobile.util.images.CourseCardUtils;


public abstract class MyCourseAdapter extends BaseListAdapter<EnrolledCoursesResponse> {
    private long lastClickTime;

    public MyCourseAdapter(Context context, IEdxEnvironment environment) {
        super(context, CourseCardViewHolder.LAYOUT, environment);
        lastClickTime = 0;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void render(BaseViewHolder tag, final EnrolledCoursesResponse enrollment) {
        final CourseCardViewHolder holder = (CourseCardViewHolder) tag;

        final CourseEntry courseData = enrollment.getCourse();
        holder.setPadding(tag.position == 0);
        holder.setCourseTitle(courseData.getName());
        holder.setCourseImage(courseData.getCourse_image(environment.getConfig()));

        if (enrollment.getCourse().hasUpdates()) {
            holder.setHasUpdates(courseData, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAnnouncementClicked(enrollment);
                }
            });
        } else {
            holder.setDescription(
                    courseData.getDescription(),
                    CourseCardUtils.getFormattedDate(getContext(), courseData)
            );
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        return new CourseCardViewHolder(convertView);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {
        //This time is checked to avoid taps in quick succession
        final long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentTime;
            EnrolledCoursesResponse model = getItem(position);
            if (model != null) onItemClicked(model);
        }
    }

    public abstract void onItemClicked(EnrolledCoursesResponse model);

    public abstract void onAnnouncementClicked(EnrolledCoursesResponse model);
}
