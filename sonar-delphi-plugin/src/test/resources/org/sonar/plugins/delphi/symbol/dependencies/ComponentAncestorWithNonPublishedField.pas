unit ComponentAncestorWithNonPublishedField;

interface

uses
    Vcl.Controls
  , System.Classes
  ;
  
type
  TFooControl = class(TCustomControl)
  end;

  TFoo = class(TObject)
  public
    FControl: TFooControl;
  end;

implementation

end.