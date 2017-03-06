package ge.altasoft.gia.cha.thermostat;

import android.support.v7.widget.RecyclerView;

import ge.altasoft.gia.cha.ChaActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.classes.ChaFragment;
import ge.altasoft.gia.cha.classes.ChaWidget;
import ge.altasoft.gia.cha.classes.ItemViewHolder;
import ge.altasoft.gia.cha.classes.OnStartDragListener;
import ge.altasoft.gia.cha.views.RoomSensorView;

public class FragmentRoomSensors extends ChaFragment implements OnStartDragListener {

    public FragmentRoomSensors() {
    }

    @Override
    protected boolean canReorder() {
        return true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_room_sensors;
    }

    @Override
    protected RecyclerView.Adapter<ItemViewHolder> getRecycleAdapter() {
        return new RoomSensorRecyclerListAdapter(this);
    }

    @Override
    public void checkSensors() {
        if (rootView != null) {
            for (int i = 0; i < recyclerView.getChildCount(); i++)
                ((ChaWidget) recyclerView.getChildAt(i)).refresh();
        }
    }

    @Override
    public void saveWidgetOrders() {
        if (ThermostatControllerData.Instance.widgetOrderChanged()) {
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                ChaWidget w = getWidgetAt(recyclerView, i);
                if (w != null)
                    ThermostatControllerData.Instance.roomSensors(w.getWidgetId(), false).setOrder(i);
            }

            ((ChaActivity) getActivity()).publish("chac/ts/settings/rs/names", ThermostatControllerData.Instance.encodeRoomSensorNamesAndOrder(), false);
            ThermostatControllerData.Instance.setWidgetOrderChanged(false);
        }
    }


    @Override
    public void rebuildUI(boolean isStart) {
        if ((rootView == null) || (ThermostatControllerData.Instance == null) || !ThermostatControllerData.Instance.haveRoomSensorsSettings())
            return;

        hideWaitingScreen();

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            RoomSensorView w = (RoomSensorView) recyclerView.getChildAt(i);
            RoomSensorData data = ThermostatControllerData.Instance.getRoomSensorFromUIIndex(i);
            if ((w != null) && (data != null)) {
                data.setOrder(i);
                w.setSensorData(data);
            }
        }
    }


    public void drawState(int id) {
        if (rootView == null)
            return;

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            ChaWidget w = (ChaWidget) recyclerView.getChildAt(i);
            if ((w != null) && (w.getWidgetId() == id)) {
                w.refresh();
                break;
            }
        }
    }
}