unit ComponentAncestorDeclaredInImplementation;

interface

implementation

uses
    Vcl.Controls
  , System.Classes
  ;
  
type
  TFooControl = class(TCustomControl)
  end;
  TFooComponent = class(TCustomControl)
    FControl: TFooControl;
  end;

end.