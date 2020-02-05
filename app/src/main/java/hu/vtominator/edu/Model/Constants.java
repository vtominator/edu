package hu.vtominator.edu.Model;

public class Constants {
    public static final String ROOT_URL = "http://192.168.1.128/edu/v1/";

    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$";
    public static final String EMAIL_PATTERN = "([a-z0-9][-a-z0-9_\\+\\.]*[a-z0-9])@([a-z0-9][-a-z0-9\\.]*[a-z0-9]\\.(arpa|root|aero|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cu|cv|cx|cy|cz|de|dj|dk|dm|do|dz|ec|ee|eg|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|me|mc|md|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|sl|sm|sn|so|sr|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|um|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|yu|za|zm|zw)|([0-9]{1,3}\\.{3}[0-9]{1,3}))";


    public static final String URL_CREATEUSER = ROOT_URL+"createUser.php";
    public static final String URL_LOGINUSER = ROOT_URL+"loginUser.php";
    public static final String URL_UPLOADPICTURE = ROOT_URL+"uploadPicture.php";

    public static final String URL_SETEVENTS = ROOT_URL+"addEvent.php";
    public static final String URL_GETALLEVENTS = ROOT_URL+"getAllEvents.php";
    public static final String URL_UPLOAD_EVENTPICTURE = ROOT_URL+"uploadEventPicture.php";

    public static final String URL_SETPINNED = ROOT_URL+"addPinned.php";

    public static final String URL_SETFAVORITE = ROOT_URL+"addFavorite.php";
    public static final String URL_DELETEFAVORITE = ROOT_URL+"deleteFavorite.php";
    public static final String URL_GETFAVORITES = ROOT_URL+"getFavorites.php";

    public static final String URL_GETLIKES = ROOT_URL+"getLikes.php";
    public static final String URL_GETCOMMENTCOUNT = ROOT_URL+"getCommentCount.php";

    public static final String URL_DELETEEVENT = ROOT_URL+"deleteEvent.php";

    public static final String URL_ADDCOMMENT = ROOT_URL+"addComment.php";
    public static final String URL_GETALLCOMMENTS = ROOT_URL+"getAllComments.php";


}
