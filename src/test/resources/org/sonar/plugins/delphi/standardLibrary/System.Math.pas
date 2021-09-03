unit System.Math;

interface

function IntPower(const Base: Single; const Exponent: Integer): Single; overload;
function IntPower(const Base: Double; const Exponent: Integer): Double; overload;
function IntPower(const Base: Extended; const Exponent: Integer): Extended; overload;

function Power(const Base, Exponent: Extended): Extended; overload;
function Power(const Base, Exponent: Double): Double; overload;
function Power(const Base, Exponent: Single): Single; overload;

implementation

end.
