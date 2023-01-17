unit ComponentAncestorWithPublishedFieldInNonComponentType;

interface

uses
    Vcl.Controls
  , System.Classes
  ;
  
type
  TFooControl = class(TCustomControl)
  end;

  TFoo = class(TObject)
    FControl: TFooControl;
  end;
  
implementation

end.